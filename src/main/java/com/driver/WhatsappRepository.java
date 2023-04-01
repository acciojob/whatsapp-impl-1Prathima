package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;


    HashMap<String, User> userDb = new HashMap<>();
    HashMap<Integer, Message> messageDb = new HashMap<>();

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(userDb.containsKey(mobile)){
            throw new Exception("User already exists");
        }
        else {
            User user = new User(name, mobile);
            userDb.put(mobile, user);
            return "SUCCESS";
        }
    }

    public Group createGroup(List<User> users){
        String groupName;
        if(users.size()==2){
            groupName = users.get(1).getName();
        }
        else{
            customGroupCount++;
            groupName = "group"+customGroupCount;
        }
        Group group = new Group(groupName, users.size());
        groupUserMap.put(group,users);
        return group;
    }

    public int createMessage(String content){
        messageId++;
        Message message  = new Message(messageId,content,new java.sql.Date(System.currentTimeMillis()));
        messageDb.put(messageId,message);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        boolean flag = false;
        if(groupUserMap.containsKey(group)){
            for(User user : groupUserMap.get(group)){
                if(user.equals(sender)){
                    flag = true;
                    List<Message> messages = groupMessageMap.get(group);
                    if(messages == null){
                        messages = new ArrayList<>();
                    }
                    messages.add(message);
                    groupMessageMap.put(group, messages);
                }
            }
            if(flag == false){
                throw new Exception("You are not allowed to send message");
            }
        }
        else{
            throw new Exception("Group does not exist");
        }
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        boolean flag = false;
        if(groupUserMap.containsKey(group)){
            if(adminMap.get(group) != approver && adminMap.get(group)!=null){
                throw new Exception("Approver does not have rights");
            }
            else{
                for(List<User> userr : groupUserMap.values()){
                    if(userr.equals(user)){
                        flag = true;
                        adminMap.put(group, user);
                    }
                }
                if(flag == false){
                    throw new Exception("User is not a participant");
                }
            }
        }
        else{
            throw new Exception("Group does not exist");
        }
        return "SUCCESS";
    }
}
