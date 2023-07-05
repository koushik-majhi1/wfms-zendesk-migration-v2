package com.byjus.zendeskMigration.repository;

import com.byjus.zendeskMigration.pojo.MigratedTicketInfo;
import com.byjus.zendeskMigration.pojo.Root;
import com.byjus.zendeskMigration.pojo.WFMSObjects;
import com.byjus.zendeskMigration.util.FileHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author koushik.majhi1@byjus.com
 * @created 04/07/2023 - 5:39 pm
 */

@Getter
@Component
public class MigrationRepository {

    private final FileHelper fileHelper;

    private HashMap<String, Root.Trigger> triggers;
    private HashMap<String, Root.Trigger> wfmsTriggers;
    private HashMap<String, Root.User> users;
    private HashMap<String, Root.GroupMembership> group_memberships;
    private HashMap<String, Root.Group> groups;
    private HashMap<String, WFMSObjects.Project> wfmsProjects;
    private HashMap<String, WFMSObjects.Group> wfmsGroups;
    private HashMap<String, Root.TicketForm> forms;
    private HashMap<String, WFMSObjects.Form> wfmsForms;
    private ConcurrentHashMap<String, MigratedTicketInfo> migratedTicketInfo;
    private Map<String, String> projectGroups;
    private HashMap<String, Integer> mapFormsCategory;
    private HashMap<String, Root.TicketField> fields;
    private HashMap<Integer, String> unassignedGroups;

    public MigrationRepository(final FileHelper fileHelper) throws IOException {
        this.fileHelper = fileHelper;
        loadRepository();
    }

    private void loadRepository() throws IOException {

        System.out.println("Loading MigrationRepository");
        triggers = fileHelper.getData(
                fileHelper.getFilePath("triggers"),
                new TypeToken<ArrayList<Root.Trigger>>() {
                }.getType());

        wfmsTriggers   = fileHelper.getData(
                fileHelper.getCompiledFilePath("triggers"),
                new TypeToken<ArrayList<Root.Trigger>>() {
                }.getType());

        if(users==null ) {
            users = fileHelper.getData(
                    fileHelper.getFilePath("users"),
                    new TypeToken<ArrayList<Root.User>>() {
                    }.getType());
        }

        group_memberships = fileHelper.getData(
                fileHelper.getFilePath("group_memberships"),
                new TypeToken<ArrayList<Root.GroupMembership>>() {
                }.getType());
        groups = fileHelper.getData(
                fileHelper.getFilePath("groups"),
                new TypeToken<ArrayList<Root.Group>>() {
                }.getType());

        wfmsGroups = fileHelper.getData(
                fileHelper.getCompiledFilePath("Groups"),
                new TypeToken<ArrayList<WFMSObjects.Group>>() {
                }.getType());

        wfmsProjects = fileHelper.getData(
                fileHelper.getCompiledFilePath("Projects"),
                new TypeToken<ArrayList<WFMSObjects.Project>>() {
                }.getType());

        forms = fileHelper.getData(
                fileHelper.getFilePath("ticket_forms"),
                new TypeToken<ArrayList<Root.TicketForm>>() {
                }.getType());

        wfmsForms = fileHelper.getData(
                fileHelper.getCompiledFilePath("Forms"),
                new TypeToken<ArrayList<WFMSObjects.Form>>() {
                }.getType());

        migratedTicketInfo= fileHelper.getConcurrentData(
                fileHelper.getCompiledFilePath("MigratedTicketInfo"),
                new TypeToken<ArrayList<MigratedTicketInfo>>() {
                }.getType());

        if (Files.exists(Path.of(fileHelper.getCompiledFilePath("ProjectGroups")))) {
            projectGroups = (new Gson()).fromJson(Files.readString(Path.of(fileHelper.getCompiledFilePath("ProjectGroups"))), new TypeToken<Map<String, String>>() {
            }.getType());
        }

        mapFormsCategory = new HashMap<String, Integer>();
        for (var form : forms.values()) {
            for (var project : wfmsProjects.values()) {
                if (Arrays.stream(project.description.split(" ~ ")).toList().contains(form.name.trim())) {
                    mapFormsCategory.put(form.id, project.projectId);
                }
            }
        }

        fields = fileHelper.getData(
                fileHelper.getFilePath("ticket_fields"),
                new TypeToken<ArrayList<Root.TicketField>>() {
                }.getType());

        unassignedGroups = new HashMap<>();
        for (var item : wfmsGroups.values()) {
            if (item.groupName.startsWith("Unassigned")) {
                unassignedGroups.put(item.projectId, item.groupName);
            }
        }
        System.out.println("Completed Loading MigrationRepository");
    }
}
