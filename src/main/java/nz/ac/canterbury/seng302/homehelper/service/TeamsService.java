package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.persistence.EntityNotFoundException;
import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.*;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.validation.TeamValidation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for handling teams.
 * Responsible for saving requests and validating beforehand.
 */
@Service
public class TeamsService {
    private final Logger log = LoggerFactory.getLogger(TeamsService.class);

    private final TeamsRepository teamsRepository;
    private final TeamValidation teamValidation;
    private final ContractorRepository contractorRepository;
    private final EmailService emailService;
    private final RenovationRecordRepository renovationRecordRepository;

    /**
     * Constructs TeamsService with necessary dependencies.
     * @param teamsRepository Repository for saving teams.
     * @param teamValidation Service used to validate team requests.
     */
    @Autowired
    public TeamsService(TeamsRepository teamsRepository, TeamValidation teamValidation, ContractorRepository contractorRepository, EmailService emailService,
                        RenovationRecordRepository renovationRecordRepository) {
        this.teamsRepository = teamsRepository;
        this.teamValidation = teamValidation;
        this.contractorRepository = contractorRepository;
        this.emailService = emailService;
        this.renovationRecordRepository = renovationRecordRepository;
    }

    /**
     * Get the team associated with a given renovation record.
     * @param renovationRecord The renovation record to get the team associated with it.
     * @return Team associated with given record.
     */
    public Team getTeamFromRenovation(RenovationRecord renovationRecord) {
        return teamsRepository.findByRenovationRecord(renovationRecord);
    }

    /**
     * Creates a new team
     * @param teamRecord the renovation record with which the team was associated
     * @param teamRequestDTO the request DTO containing the info about the skills required
     * @return the response value of the matching algorithm
     */
    public String createNewTeam(RenovationRecord teamRecord, TeamRequestDTO teamRequestDTO, boolean autoInvite) {
        Team team = new Team(teamRecord);
        team.setAutomaticFilling(autoInvite);

        List<Role> roles = createRoles(teamRequestDTO.getSkills());
        for(Role role : roles) {
            team.addRole(role);
        }

        teamsRepository.save(team);
        renovationRecordRepository.save(teamRecord);
        if (!team.hasAutomaticFilling()) return "manual";

        Location renovationLocation = teamRecord.getLocation();
        String response = assignContractorsToTeam(team, renovationLocation);
        if (Objects.equals(response, "")) {
            for (Role role : team.getRoles()) {
                role.setStatus(RoleStatus.WAITING);
            }
            teamsRepository.save(team);
            sendContractorEmails(team);
        }

        return response;
    }

    /**
     * Constructs a list of roles for the team entity.
     * @param skillNames The list of skills selected by the user
     * @return An array of roles created from each skill
     */
    public List<Role> createRoles(List<String> skillNames) {
        List<Role> roles = new ArrayList<>();
        for (String skillName : skillNames) {
            Skill skill = Skill.valueOf(skillName);
            Role role = new Role(skill);
            roles.add(role);
        }
        return roles;
    }

    /**
     * Saves a team to the repository.
     * @param team The team to be saved.
     */
    public void saveTeam(Team team) {
        teamsRepository.save(team);
    }

    /**
     * Checks whether a team already exists for the given renovation ID.
     * @param id The renovation record ID.
     * @return True if a team already exists, false otherwise.
     */
    public boolean teamExists(Long id) {
        return teamsRepository.existsByRenovationRecordId(id);
    }

    /**
     * Validates a team creation request by calling all validation methods.
     * @param teamRequestDTO The DTO representing the creation request.
     * @return List containing the errors with the request, which will be empty if it's valid.
     */
    public List<String> validateTeam(TeamRequestDTO teamRequestDTO) {
        List<String> errors = new ArrayList<>();

        String  skillsTypeError =  teamValidation.validateSkills(teamRequestDTO);
        if (skillsTypeError != null) errors.add(skillsTypeError);

        String  teamSizeError = teamValidation.validateTeamSize(teamRequestDTO);
        if (teamSizeError != null) errors.add(teamSizeError);

        return errors;
    }

    public Team getTeamById(long teamId) {
        return teamsRepository.findById(teamId).orElseThrow(() -> new EntityNotFoundException("Team: " + teamId + " not found"));
    }

    /**
     * For a given team, returns a map containing all contractor ids. Can return a partially
     * or wholly empty map if some or all of the roles are unfilled.
     * and their corresponding contractors.
     * @param teamId the id of the team used
     * @return the map of contractor ids and contractors
     */
    public Map<Long, Contractor> getContractorsByTeamId(Long teamId) {
        Map<Long, Contractor> contractors = new HashMap<>();
        Team team = getTeamById(teamId);
        for (Role role : team.getRoles()) {
            if (role.getContractorId() != null) {
                Long contractorId = role.getContractorId();
                Optional<Contractor> contractor = contractorRepository.findById(contractorId);
                contractor.ifPresent(value -> contractors.put(contractorId, value));
            }
        }
        return contractors;
    }

    /**
     * Checks if a given user belongs to the team associated with a renovation record
     * @param renovationRecord the renovation record that we want to check the associated team
     * @param user the id of the user to check if they belong to the team
     * @return boolean, true if the user is a contractor and belongs to the team associated with the record
     */
    public boolean checkViewRenovationAccess(RenovationRecord renovationRecord, User user) {
        return teamsRepository.checkIfUserBelongsToRecordTeam(renovationRecord, user.getId());
    }

    /**
     * Deletes the specified {@link Team}
     * @param team The {@link Team} to delete
     */
    public void deleteTeam(Team team) {
        teamsRepository.delete(team);
    }


    /**
     * Goes through the list of contractors assigned to a team and
     * emails them, notifying them that they have an offer to join
     * a team
     * @param team the newly created team emails are being sent to
     */
    public void sendContractorEmails(Team team) {
        for (Role role : team.getRoles()) {
            try {
                Contractor recipient = contractorRepository.findById(role.getContractorId()).orElseThrow();
                String ownerName = team.getRenovationRecord().getUser().getFirstName();
                emailService.sendRequestToContractor(recipient.getEmail(), recipient.getFirstName(), ownerName,
                        team.getRenovationRecord().getName(), role.getSkill().getDisplayName(), java.util.Locale.getDefault(),team.getId());
            } catch (NoSuchElementException e) {
                log.debug("Role for skill {} has no contractor", role.getSkill());
            }
        }
    }

    /**
     * Returns a list of team requests for the given user after checking if they are a contractor.
     *
     * @param user the user to find team requests for
     * @return the list of team requests, ordered by creation date
     * @throws IllegalArgumentException if the user is not a contractor
     */
    public List<Team> getContractorTeamRequests(User user) throws IllegalArgumentException {
        if (user instanceof Contractor contractor) {
            return teamsRepository.findByRoleContractor(contractor.getId());
        } else {
            throw new IllegalArgumentException("User is not a contractor");
        }
    }

    /**
     * Returns the role the user (contractor) is assigned to. The team should already have been found by getContractorTeamRequests.
     *
     * @param user the user who already has a role assigned in the team
     * @param team the team which has a role filled by the given contractor
     * @return the Role assigned to the contractor
     * @throws ResponseStatusException if the role is not found
     */
    public Role getContractorRole(User user, Team team) throws ResponseStatusException {
        try {
            return team.getRoles().stream().filter(r -> r.getContractorId().equals(user.getId())).findFirst().orElseThrow();
        } catch (NoSuchElementException|NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        }
    }

    /**
     * Run algorithm to assign the closest available contractors to the team if possible
     * @param team team to assign contractors to
     * @param renovationLocation location of the renovation
     * @return error message if unable to fill team or empty string if able to fill team
     */
    public String assignContractorsToTeam(Team team, Location renovationLocation) {
        boolean greedySuccess = greedyAssign(team, renovationLocation);
        if (greedySuccess) {
            teamsRepository.save(team);
            return "";
        }

        Set<String> visitedStates = new HashSet<>();

        for (Role emptyRole : team.getRoles()) {
            if (emptyRole.getContractorId() == null) {
                List<Role> candidatesToShuffle = team.getRoles().stream()
                        .filter(r -> r.getContractorId() != null)
                        .filter(r ->  contractorRepository.findById(r.getContractorId())
                                .map(contractor -> contractor.getSkills().contains(emptyRole.getSkill())).orElse(false))
                        .toList();

                for (Role candidateRole : candidatesToShuffle) {
                    Contractor contractorToMove = contractorRepository.findById(candidateRole.getContractorId()).orElse(null);

                    emptyRole.setContractor(contractorToMove);
                    candidateRole.setContractor(null);

                    // Fill the vacated candidateRole recursively with cycle detection
                    if (fillRoleWithBacktracking(team, renovationLocation, candidateRole, visitedStates)) {
                        teamsRepository.save(team);
                        return "";
                    }

                    // Backtrack
                    emptyRole.setContractor(null);
                    candidateRole.setContractor(contractorToMove);
                }

                return "Unable to find available contractors to fill team";
            }
        }

        return "Unable to find available contractors to fill team";
    }

    /**
     * Fill the given empty role by querying nearest available contractor
     * excluding contractors already assigned in the team.
     * If none found, try reassigning team members recursively (backtracking).
     * Uses cycle detection to avoid infinite loops.
     * @param team team to assign contractors to
     * @param location location of the renovation
     * @param roleToFill role to fill
     * @param visitedStates Set of serialized team that have been visited
     * @return true if all roles assigned, false if any remain unassigned.
     */
    private boolean fillRoleWithBacktracking(Team team, Location location, Role roleToFill, Set<String> visitedStates) {
        String stateKey = serializeTeamAssignment(team);
        if (!visitedStates.add(stateKey)) {
            // prune
            return false;
        }

        Set<Long> assignedIds = team.getRoles().stream()
                .map(Role::getContractorId)
                .filter(contractorId -> contractorId != null && contractorId != 0L)
                .collect(Collectors.toSet());

        Set<Long> blacklistIds = new HashSet<>(team.getBlacklistIds());
        assignedIds.addAll(blacklistIds);

        Contractor candidate = findNearestContractor(roleToFill, location, assignedIds);
        if (candidate != null) {
            roleToFill.setContractor(candidate);
            return true;
        }

        // No direct candidate found, try reassigning team members recursively:
        for (Role otherRole : team.getRoles()) {
            Optional<Contractor> contractor = getContractorFromRole(otherRole);
            if (otherRole != roleToFill && contractor.isPresent()
                    && contractor.get().getSkills().contains(roleToFill.getSkill())) {

                Contractor movingContractor = contractor.get();

                // Move contractor to current empty role
                roleToFill.setContractor(movingContractor);
                otherRole.setContractor(null);

                if (fillRoleWithBacktracking(team, location, otherRole, visitedStates)) {
                    return true;
                }

                // Backtrack
                otherRole.setContractor(movingContractor);
                roleToFill.setContractor(null);
            }
        }

        return false;
    }

    /**
     * Assign nearest available contractor for each unassigned role.
     * @param team team to assign contractors to
     * @param renovationLocation location of the renovation
     * @return true if all roles assigned, false if any remain unassigned.
     */
    private boolean greedyAssign(Team team, Location renovationLocation) {
        Set<Long> assignedContractors = new HashSet<>(team.getBlacklistIds());
        boolean allAssigned = true;

        for (Role role : team.getRoles()) {
            Long id = role.getContractorId();
            if (id == null || id == 0L) {
                Contractor contractor = findNearestContractor(role, renovationLocation, assignedContractors);
                if (contractor == null) {
                    allAssigned = false;
                } else {
                    team.replaceRoleContractor(role, contractor);
                    assignedContractors.add(contractor.getId());
                }
            } else {
                assignedContractors.add(role.getContractorId());
            }
        }

        return allAssigned;
    }

    /**
     *  Finds the nearest available contractor for a role excluding contractors in blacklist.
     * @param role empty role to find contractor for
     * @param location renovation location
     * @param blacklist set of current contractors in team to prevent duplicates
     * @return the nearest contractor that can fill the role
     */
    private Contractor findNearestContractor(Role role, Location location, Set<Long> blacklist) {
        return contractorRepository.findNearestWithinDistanceExcluding(
                location.getLatitude(),
                location.getLongitude(),
                role.getSkill().toString(),
                200,
                blacklist.isEmpty() ? null : blacklist
        );
    }

    /**
     * Serialize team assignments as a string key for cycle detection.
     * @param team team to serialize
     * @return serialized team
     */
    private String serializeTeamAssignment(Team team) {
        return team.getRoles().stream()
                .map(role -> {
                    Optional<Contractor> c = getContractorFromRole(role);
                    return role.getSkill() + ":" + ((c.isEmpty()) ? "null" : c.get().getId());
                })
                .collect(Collectors.joining("|"));
    }

    private Optional<Contractor> getContractorFromRole(Role role) {
        return (role.getContractorId() != null) ?
                contractorRepository.findById(role.getContractorId()) :
                Optional.empty();
    }

    /**
     * Sets the contractor Id and isAccepted of the given role of the given team to null and false respectively.
     * @param team the team the role is a part of
     * @param contractor the contractor being removed from the team
     */
    public void deleteContractorFromTeam(Team team, Contractor contractor) {
        for (Role role : team.getRoles()) {
            if (role.getContractorId() != null && role.getContractorId().equals(contractor.getId())) {
                role.removeContractor();
                role.setStatus(RoleStatus.UNFILLED);
            }
        }
        teamsRepository.save(team);
        runAlgorithmAgain(team, team.getRenovationRecord().getLocation());
    }

    /**
     * Runs the algorithm again, makes a set of current members before and after to check for new members.
     * If new members are found they are notified by email.
     * @param team The team to re-run the algorithm on.
     * @param renovationLocation The location of the renovation record associated with the Team.
     */
    public void runAlgorithmAgain(Team team, Location renovationLocation) {
        if (!team.hasAutomaticFilling()) return;

        Set<Long> beforeIds = team.getRoles().stream()
                .map(Role::getContractorId)
                .filter(id -> id != null && id != 0L)
                .collect(Collectors.toSet());

        assignContractorsToTeam(team, renovationLocation);

        Set<Long> newMemberIds = team.getRoles().stream()
                .map(Role::getContractorId)
                .filter(id -> id != null && id != 0L)
                .collect(Collectors.toSet());
        newMemberIds.removeAll(beforeIds);

        if (!newMemberIds.isEmpty()) {
            sendContractorEmailsTo(team, newMemberIds);
        }
    }

    /**
     * Sends contractors team invite emails, to specified contractors.
     * @param team The team the contractors belong to.
     * @param contractorIds The list of contractor Ids that need to be sent the invite email.
     */
    private void sendContractorEmailsTo(Team team, Set<Long> contractorIds) {
        String ownerName = team.getRenovationRecord().getUser().getFirstName();

        Map<Long, Role> roleByContractorId = new HashMap<>();

        for (Role role : team.getRoles()) {
            Long contractorId = role.getContractorId();
            if (contractorId != null && contractorId != 0L) {
                roleByContractorId.putIfAbsent(contractorId, role);
            }
        }

        for (Long id : contractorIds) {
            Contractor recipient = contractorRepository.findById(id).orElseThrow();
            Role role = roleByContractorId.get(id);

            emailService.sendRequestToContractor(recipient.getEmail(), recipient.getFirstName(), ownerName,
                    team.getRenovationRecord().getName(), role.getSkill().getDisplayName(), java.util.Locale.getDefault(),team.getId());
        }
    }
}
