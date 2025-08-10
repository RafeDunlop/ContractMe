package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.validation.TeamValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service class for handling teams.
 * Responsible for saving requests and validating beforehand.
 */
@Service
public class TeamsService {

    private final TeamsRepository teamsRepository;
    private final TeamValidation teamValidation;
    private final ContractorRepository contractorRepository;


    /**
     * Constructs TeamsService with necessary dependencies.
     * @param teamsRepository Repository for saving teams.
     * @param teamValidation Service used to validate team requests.
     */
    @Autowired
    public TeamsService(TeamsRepository teamsRepository, TeamValidation teamValidation, ContractorRepository contractorRepository) {
        this.teamsRepository = teamsRepository;
        this.teamValidation = teamValidation;
        this.contractorRepository = contractorRepository;
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

    /**
     * Assigns available contractors to the given team's roles based on proximity
     * to a specified renovation location, ensuring that no contractor is assigned
     * more than once.
     * @param team               the team whose roles need contractors assigned
     * @param renovationLocation the location of the renovation used to determine contractor proximity
     * @return an empty string if all roles were successfully assigned contractors,
     *         or an error message if one or more roles could not be filled
     */
    public String assignContractorsToTeam(Team team, Location renovationLocation) {
        // Store all contractor's id in list to prevent duplicates
        Set<Long> assignedContractors = new HashSet<>();

        for (Role role : team.getRoles()) {
            Contractor availableContractor = findContractor(role, renovationLocation, assignedContractors);

            if (availableContractor != null) {
                team.replaceRoleContractor(role, availableContractor);
                assignedContractors.add(availableContractor.getId());
            } else {
                return "Unable to find available contractors to fill team";
            }
        }
        return "";
    }

    /**
     * Find the closest available and suitable contractor for a given role
     * @param role the desired role for the position in the team
     * @param renovationLocation location of the renovation
     * @return the closest available and suitable contractor for a given role
     */
    private Contractor findContractor(Role role, Location renovationLocation, Set<Long> blacklist) {
        double renovationLat = renovationLocation.getLatitude();
        double renovationLon = renovationLocation.getLongitude();

        return contractorRepository.findNearestWithinDistanceExcluding(
                renovationLat, renovationLon, role.getSkill().getDisplayName(), 200, blacklist
        );
    }
}
