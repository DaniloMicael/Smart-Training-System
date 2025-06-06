package sts.backend.core_app.services.business;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import sts.backend.core_app.dto.team.TeamsInfoView;
import sts.backend.core_app.dto.team.PlayersAvailableRealTimeInfo;
import sts.backend.core_app.dto.team.RegistrationCodeString;
import sts.backend.core_app.dto.team.SensorPlayerInfo;
import sts.backend.core_app.dto.team.SensorPlayerView;
import sts.backend.core_app.dto.team.SensorTeamInfo;
import sts.backend.core_app.dto.team.TeamCreation;
import sts.backend.core_app.dto.team.TeamMemberRegistration;
import sts.backend.core_app.dto.team.TeamMembersResponse;
import sts.backend.core_app.dto.team.TeamDirectorsView;
import sts.backend.core_app.exceptions.ResourceNotFoundException;
import sts.backend.core_app.models.Player;
import sts.backend.core_app.models.PlayerSensor;
import sts.backend.core_app.models.RegistrationCode;
import sts.backend.core_app.models.Sensor;
import sts.backend.core_app.models.Team;
import sts.backend.core_app.services.analysis.interfaces.BasicDataAnalysis;
import sts.backend.core_app.services.analysis.interfaces.RealTimeAnalysis;

@Service
public class TeamService {
    public static final int REGISTRATION_CODE_EXPIRATION_MINUTES = 15;

    private final BasicDataAnalysis basicDataAnalysis;
    private final RealTimeAnalysis realTimeAnalysis;

    public TeamService(BasicDataAnalysis basicDataAnalysis, RealTimeAnalysis realTimeAnalysis) {
        this.basicDataAnalysis = basicDataAnalysis;
        this.realTimeAnalysis = realTimeAnalysis;
    }

    public Team createTeam(TeamCreation teamCreation) throws ResourceNotFoundException {
        Team team = new Team();
        team.setName(teamCreation.getName());
        return basicDataAnalysis.createTeam(team);
    }

    public RegistrationCode generateNewRegistrationCode(TeamMemberRegistration teamMemberRegistration) throws ResourceNotFoundException {
        RegistrationCode registrationCode = new RegistrationCode();
        registrationCode.setTeam(basicDataAnalysis.getTeamById(teamMemberRegistration.getTeamId()));
        registrationCode.setName(teamMemberRegistration.getName());
        registrationCode.setCode(UUID.randomUUID().toString());
        registrationCode.setUserTypeId(teamMemberRegistration.getUserTypeId());
        registrationCode.setProfilePictureUrl(teamMemberRegistration.getProfilePictureUrl());
        
        registrationCode.setExpirationTime(LocalDateTime.now().plusMinutes(REGISTRATION_CODE_EXPIRATION_MINUTES));
        
        return basicDataAnalysis.createRegistrationCode(registrationCode);
    }

    public RegistrationCode refreshRegistrationCode(RegistrationCodeString code) throws ResourceNotFoundException {
        RegistrationCode registrationCode = basicDataAnalysis.getRegistrationCode(code.getCode());
        registrationCode.setCode(UUID.randomUUID().toString());
        registrationCode.setExpirationTime(LocalDateTime.now().plusMinutes(REGISTRATION_CODE_EXPIRATION_MINUTES));
        return basicDataAnalysis.createRegistrationCode(registrationCode);
    }
    
    public RegistrationCode claimRegistrationCode(String code) throws ResourceNotFoundException {
        RegistrationCode registrationCode = basicDataAnalysis.getRegistrationCode(code);

        if (registrationCode.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("Registration code expired");
        }
        
        basicDataAnalysis.deleteRegistrationCode(registrationCode);

        return registrationCode;
    }

    public Set<TeamsInfoView> getTeamsInfo() throws ResourceNotFoundException {
        return basicDataAnalysis.getTeamsInfo();
    }

    public void deleteTeam(Long teamId) {
        basicDataAnalysis.deleteTeam(teamId);
    }

    public List<PlayersAvailableRealTimeInfo> getPlayersAvailableRealTimeInfo(Long teamId) throws ResourceNotFoundException {
        return realTimeAnalysis.getPlayersAvailableRealTimeInfo(teamId);
    }

    public void deleteRegistrationCode(RegistrationCodeString code) throws ResourceNotFoundException {
        RegistrationCode registrationCode = basicDataAnalysis.getRegistrationCode(code.getCode());
        basicDataAnalysis.deleteRegistrationCode(registrationCode);
    }

    public List<TeamMembersResponse> getTeamMembers(Long teamId) {
        return basicDataAnalysis.getTeamMembers(teamId);
    }

    public Set<TeamDirectorsView> getTeamDirectors(Long teamId) throws ResourceNotFoundException {
        return basicDataAnalysis.getTeamDirectors(teamId);
    }

    public Set<SensorPlayerView> getSensors(Long teamId) throws ResourceNotFoundException {
        return basicDataAnalysis.getSensors(teamId);
    }

    public void deleteSensors(Long sensorId) {
        basicDataAnalysis.deleteSensor(sensorId);
    }

    public List<Player> getPlayersWithoutSensorsByTeamId(Long teamId) throws ResourceNotFoundException {
        return basicDataAnalysis.getPlayersWithoutSensorsByTeamId(teamId);
    }

    public Sensor assignSensor(SensorTeamInfo sensorTeamInfo) throws ResourceNotFoundException {
        return basicDataAnalysis.assignSensor(sensorTeamInfo);
    }

    public PlayerSensor assignPlayerToSensor(SensorPlayerInfo sensorPlayerInfo) throws ResourceNotFoundException {
        return basicDataAnalysis.assignPlayerToSensor(sensorPlayerInfo);
    }

    public void unassignPlayerFromSensor(SensorPlayerInfo sensorPlayerInfo) throws ResourceNotFoundException {
        basicDataAnalysis.unassignPlayerFromSensor(sensorPlayerInfo);
    }

}
