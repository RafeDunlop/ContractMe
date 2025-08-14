package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.validation.TeamValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service class for handling teams.
 * Responsible for saving requests and validating beforehand.
 */
@Service
public class TeamsService {

    private final TeamsRepository teamsRepository;
    private final TeamValidation teamValidation;


    /**
     * Constructs TeamsService with necessary dependencies.
     * @param teamsRepository Repository for saving teams.
     * @param teamValidation Service used to validate team requests.
     */
    @Autowired
    public TeamsService(TeamsRepository teamsRepository, TeamValidation teamValidation) {
        this.teamsRepository = teamsRepository;
        this.teamValidation = teamValidation;
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

    public boolean acceptContractor(Role role, Contractor contractor, Team team) {
        Contractor roleContractor = role.getContractor();
        if (roleContractor != null && (Objects.equals(contractor.getId(), roleContractor.getId()))) {
            role.setAccepted(true);
            teamsRepository.save(team);
            return true;
        } else {
            throw new IllegalStateException("The given contractor is not assigned to the given role.");
        }
    }

    public boolean declineContractor(Role role, Contractor contractor, Team team) {
        Contractor roleContractor = role.getContractor();
        if (roleContractor != null && (Objects.equals(contractor.getId(), roleContractor.getId()))) {
            role.setAccepted(false);
            role.setContractor(null);
            teamsRepository.save(team);
            return true;
        } else {
            throw new IllegalStateException("The given contractor is not assigned to the given role.");
        }
    }
}
