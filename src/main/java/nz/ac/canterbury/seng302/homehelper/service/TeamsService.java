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
import java.util.List;

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
     * Find the closest available and suitable contractor for a given role
     * @param role the desired role for the position in the team
     * @param currentTeam the current team
     * @param renovationLocation location of the renovation
     * @return the closest available and suitable contractor for a given role
     */
    private Contractor findContractor(Role role, Team currentTeam, Location renovationLocation) {
        double renovationLat = renovationLocation.getLatitude();
        double renovationLon = renovationLocation.getLongitude();

        // Create blacklist of current Contractors in team to prevent duplicates
        List<Long> blacklist = new ArrayList<>();
        for (Role teamMember : currentTeam.getRoles()) {
            if (teamMember.getContractor() != null) {
                blacklist.add(teamMember.getContractor().getId());
            }
        }

        return contractorRepository.findNearestWithinDistanceExcluding(
                renovationLat, renovationLon, role.getSkill().getDisplayName(), 200, blacklist
        );
    }

}
