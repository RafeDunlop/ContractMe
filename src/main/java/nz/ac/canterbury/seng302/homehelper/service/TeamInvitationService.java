package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TeamInvitationService {

    private TeamsRepository teamsRepository;

    @Autowired
    public TeamInvitationService(TeamsRepository teamsRepository) {
        this.teamsRepository = teamsRepository;
    }

    public void acceptContractor(Contractor contractor, Team team) {
        Role role = findAssignedRole(team, contractor);
        if (role.isAccepted()) {
            return;
        }

        role.setAccepted(true);
        teamsRepository.save(team);
    }

    public void declineContractor(Contractor contractor, Team team) {
        Role role = findAssignedRole(team, contractor);
        role.setAccepted(false);
        role.setContractor(null);
        teamsRepository.save(team);
    }

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

    public boolean linkExpired(Contractor contractor, Team team) {
        return getMatchedRoles(contractor, team).isEmpty();
    }

    private List<Role> getMatchedRoles(Contractor contractor, Team team) {
        return team.getRoles().stream()
                .filter(r -> r.getContractor() != null && Objects.equals(r.getContractor().getId(), contractor.getId()))
                .toList();
    }
}
