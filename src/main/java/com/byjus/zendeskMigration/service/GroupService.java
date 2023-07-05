package com.byjus.zendeskMigration.service;

import com.byjus.zendeskMigration.enums.WEB_CLIENT;
import com.byjus.zendeskMigration.factory.WebClientFactory;
import com.byjus.zendeskMigration.pojo.IName;
import com.byjus.zendeskMigration.pojo.Root;
import com.byjus.zendeskMigration.pojo.WFMSObjects;
import com.byjus.zendeskMigration.pojo.response.external.ExternalApiResponse;
import com.byjus.zendeskMigration.pojo.response.external.GetGroupsResponse;
import com.byjus.zendeskMigration.pojo.response.external.Group;
import com.byjus.zendeskMigration.repository.MigrationRepository;
import com.byjus.zendeskMigration.util.FileHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import okhttp3.internal.http2.StreamResetException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author koushik.majhi1@byjus.com
 * @created 04/07/2023 - 4:58 pm
 */

@Service
public class GroupService {

    private final MigrationRepository migrationRepository;
    private final FileHelper fileHelper;
    private final WebClientFactory webClientFactory;
    private final ObjectMapper mapper;

    private static OkHttpClient client = new OkHttpClient().newBuilder()
                                                           .connectTimeout(50, TimeUnit.SECONDS)
                                                           .writeTimeout(50, TimeUnit.SECONDS)
                                                           .build();

    static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    static String SessionToken = "b723ca3d9cde199af380da30966d794833c8b99b8fa834fa4b2f1ebba7ab1f50";
    static String projectId = "999990";
    @Autowired
    public GroupService(final MigrationRepository migrationRepository,
                        final FileHelper fileHelper,
                        final WebClientFactory webClientFactory,
                        final ObjectMapper mapper) throws Exception {
        this.migrationRepository = migrationRepository;
        this.fileHelper = fileHelper;
        this.webClientFactory = webClientFactory;
        this.mapper = mapper;
//        SaveWFMSModels();
//        removeGroup();
    }

    private void removeGroup() {
        WebClient client = webClientFactory.getInstance(WEB_CLIENT.CONFIGURATION);
        ExternalApiResponse externalApiResponse = client
                .get()
                .uri("wfms/groupManagement/v1/groups?projectId=" + projectId+ "&pageSize=20&pageNumber=0")
                .header("Sessiontoken", SessionToken)
                .header("projectId", projectId)
                .retrieve()
                .bodyToMono(ExternalApiResponse.class)
                .block();
        Object data = externalApiResponse.getData();

        List<String> groupIds = new ArrayList<>();
        ((ArrayList) data).forEach(
                x ->{
                    Group group = mapper.convertValue(((LinkedHashMap) x).get("group"), Group.class);
                    groupIds.add(group.getGroupId());
                }
        );


        for (String groupId : groupIds) {
            client.delete().uri("wfms/groupManagement/v1/groups/" + groupId)
                  .header("Sessiontoken", SessionToken)
                  .header("projectId", projectId).retrieve().bodyToMono(Object.class).block();
        }
        System.out.println("groups deleted successfully deleted");
    }

    private void SaveWFMSModels() throws Exception {
        try {
//            ModifyData();
//            GenerateWFMSModels();
            SaveInWFMS();
        } catch (Exception ex) {
            System.out.println(ex.toString() + ex.getStackTrace());
            throw ex;
        }
        System.out.println("SaveWFMSModels Done");
    }

    private void SaveInWFMS() throws Exception {
        SaveGroups();
    }

    private void SaveGroups() throws Exception {

        String ProjectID = projectId;

        HashMap<String, WFMSObjects.Group> wfmsGroups = migrationRepository.getWfmsGroups();
        try {
            for (var item : wfmsGroups.values()) {
                if(String.valueOf(item.projectId).equals(ProjectID)) {
                    String response = SaveItemsInWFMS("wfms-configuration-management/wfms/groupManagement/v1/groups", item, String.valueOf(item.projectId));
                    try {
                        item.groupId = (new Gson()).fromJson(response, WFMSObjects.Response.class).data.group.groupId;
                    } catch (Exception ex) {
                        System.out.println(ex.toString() + ex.getStackTrace());
                        //throw ex;
                    }
                }
            }
            fileHelper.saveFile(fileHelper.getCompiledFilePath("Groups"), wfmsGroups.values());
        } catch (Exception ex) {
            System.out.println(ex.toString() + ex.getStackTrace());
            throw ex;
        }
    }

    private void ModifyData() throws Exception {
        AssignUsersToGroups();
        AddGroupEmail();
    }

    private void GenerateWFMSModels() throws Exception {
        CreateZendeskGroups();
    }

    private void CreateZendeskGroups() throws Exception {
        HashMap<String, Root.Group> groups = migrationRepository.getGroups();
        HashMap<String, WFMSObjects.Group> wfmsGroups = new HashMap<>();
        for (var group : groups.values()) {
            WFMSObjects.Group wfmsGroup = new WFMSObjects.Group();
            if (group.name.equals("Inside Sales STC")) {
                wfmsGroup.groupName = group.name;
            }
            wfmsGroup.groupName = group.name;
            wfmsGroup.description = group.description;
            if (group.groupEmail != null) {
                wfmsGroup.groupEmail = String.join(",", group.groupEmail);
            } else {
                wfmsGroup.groupEmail = "";
            }
            wfmsGroup.agent = new ArrayList<>();
            group.ownerEmail = "";
            if (group.users != null) {
                for (var user : group.users) {
                    if (group.ownerEmail.equals("")) {
                        group.ownerEmail = user.email;
                    }
                    if (user.role.equals("admin")) {
                        group.ownerEmail = user.email;
                    }
                    WFMSObjects.Group.Agent agent = new WFMSObjects.Group.Agent();
                    agent.email = user.email;
                    FillNames(user.name, agent);
                    wfmsGroup.agent.add(agent);
                }
            }
            if (group.ownerEmail.equals("")) {
                group.ownerEmail = "koushik.majhi1@byjus.com";
            }

            wfmsGroup.ownerEmail = group.ownerEmail;
            wfmsGroups.put(wfmsGroup.getId(), wfmsGroup);
        }
        FixProjectIds(wfmsGroups);
        fileHelper.saveFile(fileHelper.getFilePath("groups"), groups.values());
        fileHelper.saveFile(fileHelper.getCompiledFilePath("Groups"), wfmsGroups.values());
    }

    private static void FillNames(String userName, IName agent) {
        var name = userName.split(" ");
        agent.setFirstName(name[0]);
        agent.setLastName("");
        if (name.length > 1) {
            agent.setLastName(name[1]);
        }
    }

    private void FixProjectIds(HashMap<String, WFMSObjects.Group> groups) throws Exception {
        try {

            String ProjectPrefix = "";
            HashMap<String, WFMSObjects.Project> wfmsProjects = migrationRepository.getWfmsProjects();
            Map<String, String> projectGroups = migrationRepository.getProjectGroups();
            HashMap<String, Integer> projectss = new HashMap<>();
            for (var project : wfmsProjects.values()) {
                projectss.put(project.name, project.projectId);
            }
            var list = new ArrayList<>();
            for (var group : groups.values()) {
                if (projectGroups.containsKey(group.groupName) == false) {
                    list.add(group.groupName);
                } else if (projectss.containsKey(projectGroups.get(group.groupName) + ProjectPrefix)) {
                    group.projectId = projectss.get(projectGroups.get(group.groupName) + ProjectPrefix);
                } else {
                    System.out.println("Group not found : " + projectGroups.get(group.groupName));
                }
            }
            for (var group : list) {
                groups.remove(group);
            }
            for (var project : wfmsProjects.values()) {
                WFMSObjects.Group group = new WFMSObjects.Group();
                group.projectId = project.projectId;
                group.groupName = getUnassignedGroupName(project.name);
                group.description = getUnassignedGroupName(project.name);
                group.ownerEmail = "aman.seth@byjus.com";
                group.groupEmail = "";
                HashSet<String> agentEmailAddress  = new HashSet<>();
                group.agent = new ArrayList<>();
                for(var v : groups.values()) {
                    if(v.projectId==project.projectId) {
                        for (var agent : v.agent) {
                            if (agentEmailAddress.contains(agent.email) == false) {
                                group.agent.add(agent);
                                agentEmailAddress.add(agent.email);
                            }
                        }
                    }
                }
                groups.put(group.groupName, group);
            }
        } catch (Exception ex) {
            System.out.println(ex.toString() + ex.getStackTrace());
            throw ex;
        }
    }

    private static String getUnassignedGroupName(String projectName) {
        return "Unassigned tickets of - " + projectName;
    }


    private void AssignUsersToGroups() throws Exception {
        HashMap<String, Root.Group> groups = migrationRepository.getGroups();
        HashMap<String, Root.GroupMembership> groupMemberships = migrationRepository.getGroup_memberships();
        HashMap<String, Root.User> users = migrationRepository.getUsers();
        for (Root.Group item : groups.values()) {
            item.users = new HashSet<>();
        }

        for (var item : groupMemberships.values()) {
            var groupmembership = item;
            if (groups.containsKey(groupmembership.group_id) && users.containsKey(groupmembership.user_id)) {
                groups.get(groupmembership.group_id).users.add(users.get(groupmembership.user_id));
            }
        }
        fileHelper.saveFile(fileHelper.getFilePath("groups"), groups.values());
    }

    private void AddGroupEmail() throws Exception {
        HashMap<String, Root.Group> groups = migrationRepository.getGroups();
        HashMap<String, Root.Trigger> triggers = migrationRepository.getTriggers();
        for (var item : groups.values()) {
            item.groupEmail = new HashSet<>();
        }

        for (var item : triggers.values()) {
            if (item.active) {
                String groupId = "";
                for (var action : item.actions) {
                    if (action.field.equals("group_id")) {
                        groupId = action.value.toString();
                    }
                }
                for (var condition : item.conditions.all) {
                    if (condition.field.equals("recipient")) {
                        if (groups.containsKey(groupId)) {
                            groups.get(groupId).groupEmail.add(condition.value.toString());
                        }
                    }
                }
                for (var condition : item.conditions.any) {
                    if (condition.field.equals("recipient")) {
                        if (groups.containsKey(groupId)) {
                            groups.get(groupId).groupEmail.add(condition.value.toString());
                        }
                    }
                }
            }
        }
        fileHelper.saveFile(fileHelper.getFilePath("groups"), groups.values());
    }




    private static <T> String SaveItemsInWFMS(String endpoint, T item, String projectid) throws Exception {
        return SaveItemsInWFMS(endpoint, item, projectid, "POST");
    }

    private static <T> String SaveItemsInWFMS(String endpoint, T item, String projectid,String methodType) throws Exception {
        return SaveItemsInWFMS(endpoint, item, projectid,methodType,0);
    }

    private static <T> String SaveItemsInWFMS(String endpoint, T item, String projectid, String methodType, int rerty) throws Exception {
        try {

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, gson.toJson(item));

            Request request = new Request.Builder()
                    .url("https://h-dev-apigateway.byjus.onl/" + endpoint)
                    .method(methodType, body)
                    .addHeader("content-type", "application/json")
                    .addHeader("projectid", projectid)
                    .addHeader("authority", "h-stage-apigateway.byjus.onl")
                    .addHeader("accept", "application/json, text/plain, */*")
                    .addHeader("accept-language", "en-GB,en-US;q=0.9,en;q=0.8")
                    .addHeader("origin", "https://workflow-managment-system-staging.byjusweb.com")
                    .addHeader("referer", "https://workflow-managment-system-staging.byjusweb.com/")
                    .addHeader("sec-ch-ua", "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"macOS\"")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("sec-fetch-mode", "cors")
                    .addHeader("sec-fetch-site", "cross-site")
                    .addHeader("sessiontoken", SessionToken)
                    .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36")
                    .build();
            //System.out.println(endpoint + "   " + i);
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            //System.out.println(response);
            if (json.contains("Agent.Email is Blank") || json.contains("Agent Validation Failed") ) {
                return json;
            } else if (response.code() != 200 && response.code() != 201 && rerty < 5) {
                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
            } else if (rerty >= 5) {
                System.out.println("***************" + response.code() + "***************" + json);
            }
            return json;
        } catch (SocketException ex) {
            if (rerty < 5) {
                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
            } else {
                System.out.println(ex.toString() + ex.getStackTrace());
                return ex.toString();
            }
        } catch (SocketTimeoutException ex) {
            if (rerty < 5) {
                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
            } else {
                System.out.println(ex.toString() + ex.getStackTrace());
                return ex.toString();
            }
        } catch (StreamResetException ex) {
            client = new OkHttpClient().newBuilder()
                                       .connectTimeout(50, TimeUnit.SECONDS)
                                       .writeTimeout(50, TimeUnit.SECONDS)
                                       .build();

            if (rerty < 5) {
                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
            } else {
                System.out.println(ex.toString() + ex.getStackTrace());
                return ex.toString();
            }
        } catch (IOException ex) {

            if (rerty < 5) {
                return SaveItemsInWFMS(endpoint, item, projectid, methodType, ++rerty);
            } else {
                System.out.println(ex.toString() + ex.getStackTrace());
                return ex.toString();
            }
        } catch (Exception ex) {
            System.out.println(ex.toString() + ex.getStackTrace());
            throw ex;
        }
    }

}
