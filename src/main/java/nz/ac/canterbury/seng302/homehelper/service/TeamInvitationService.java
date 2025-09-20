package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service that handles contractor invitations to renovation teams.
 * Provides logic for accepting, declining, and checking if invite links are still valid.
 */
@Service
@EnableScheduling
public class TeamInvitationService {
    private static final int RERUNNING_FREQUENCY_MINUTES = 10;
    private final TeamsRepository teamsRepository;
    private final TeamsService teamsService;
    private static final Logger logger = LoggerFactory.getLogger(TeamInvitationService.class);

    /**
     * Creates a new instance with access to the teams repository.
     * @param teamsRepository team repository to persist changes
     */
    @Autowired
    public TeamInvitationService(TeamsRepository teamsRepository, TeamsService teamsService) {
        this.teamsRepository = teamsRepository;
        this.teamsService = teamsService;
    }

    /**
     * Scheduled task that reruns the contractor matching algorithm for all
     * incomplete teams at a fixed interval. Retrieves incomplete teams,
     * logs the count, and assigns contractors based on each team's location.
     */
    @Scheduled(fixedRate = RERUNNING_FREQUENCY_MINUTES, timeUnit = TimeUnit.MINUTES)
    public void rerunAlgorithm() {
        Set<Team> teams = teamsRepository.getIncompleteTeams();
        logger.info("Rerunning matching algorithm on {} teams",teams.size());
        for (Team team : teams) {
            Location location = team.getRenovationRecord().getLocation();
            teamsService.assignContractorsToTeam(team, location);
        }

    }

    /**
     * Accepts an invitation for a contractor to join the given team.
     * @param contractor the contractor whos accepting the invitation
     * @param team the team for the contractor to be accepted into
     * @throws IllegalStateException if the contractor is already accepted
     */
    public void acceptContractor(Contractor contractor, Team team) {
        Role role = findAssignedRole(team, contractor);
        if (role.isAccepted()) {
            throw new IllegalStateException("Invitation already accepted.");
        }

        role.setAccepted(true);
        teamsRepository.save(team);
    }

    /**
     * Declines an invitation for a contractor to join the given team.
     * Removes the contractor from the given role, removing them from the team
     * @param contractor the contractor who declines, to be removed
     * @param team the team the contractor was invited to
     * @throws IllegalStateException from findAssignedRole
     */
    public void declineContractor(Contractor contractor, Team team) {
        Role role = findAssignedRole(team, contractor);
        role.setAccepted(false);
        role.setContractor(null);
        teamsRepository.save(team);
    }

    /**
     * Finds the contractors assigned role in the given team.
     * @param team the team to search for the contractor
     * @param contractor the contractor whose role to find
     * @return the unique role assigned to the contractor
     * @throws IllegalStateException if the contractor is not in the team
     *                               or is assigned to multiple roles
     */
    public Role findAssignedRole(Team team, Contractor contractor) {
        List<Role> matches = getMatchedRoles(contractor, team);
        if (matches.isEmpty()) {
            throw new IllegalStateException("Contractor is not apart of this team.");
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Contractor is in more then one role in the team.");
        }
        return matches.get(0);
    }

    /**
     * Checks whether a link is no longer valid, used when a contractor is trying to open an invitation
     * @param contractor the contractor interacting with the link
     * @param team the team the link belongs to
     * @return boolean True if the link is expired, already accepted / declined or contractor no longer on team.
     *                 False if link is still valid.
     */
    public boolean linkExpired(Contractor contractor, Team team) {
        List<Role> matches = getMatchedRoles(contractor, team);
        if (matches.size() != 1) return true;
        return matches.get(0).isAccepted();
    }

    /**
     * Helper to get all roles that match the given contractor in a team.
     * @param contractor the contractor to match
     * @param team the team containing the roles
     * @return list of roles assigned to the contractor
     */
    private List<Role> getMatchedRoles(Contractor contractor, Team team) {
        return team.getRoles().stream()
                .filter(r -> r.getContractorId() != null && Objects.equals(r.getContractorId(), contractor.getId()))
                .toList();
    }
}
