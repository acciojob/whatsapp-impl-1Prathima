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
       if(userMobile.contains(mobile)){
           throw new Exception("User already exists");
       }
       else{
           userMobile.add(mobile);
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
            groupName = "Group"+customGroupCount;
        }
        Group group = new Group(groupName, users.size());
        groupUserMap.put(group,users);
        adminMap.put(group, users.get(0));
        return group;
    }

    public int createMessage(String content){
        messageId++;
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(groupUserMap.containsKey(group)){
            List<User> users = groupUserMap.get(group);
            if(users.contains(sender)){
                List<Message> messages = groupMessageMap.get(group);
                if(messages == null){
                    messages = new ArrayList<>();
                }
                messages.add(message);
                groupMessageMap.put(group, messages);
                senderMap.put(message, sender);
                return messages.size();
            }
            else{
                throw new Exception("You are not allowed to send message");
            }
        }
        else{
            throw new Exception("Group does not exist");
        }
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(adminMap.containsKey(group)){
            User admin = adminMap.get(group);
            if(admin.equals(approver)){
                List<User> users = groupUserMap.get(group);
                if(users.contains(user)){
                    adminMap.put(group, user);
                    return "SUCCESS";
                }
                else{
                    throw new Exception("User is not a participant");
                }
            }
            else{
                throw new Exception("Approver does not have rights");
            }
        }
        else{
            throw new Exception("Group does not exist");
        }
    }

    public int removeUser(User user) throws Exception {
        boolean userFound = false;
        boolean adminFound = false;
        for(List<User> users : groupUserMap.values()){
            if(users.contains(user)){
                userFound = true;
                for(User admin : adminMap.values()){
                    if(admin.equals(user)){
                        adminFound = true;
                        throw new Exception("Cannot remove admin");
                    }
                }
                if(adminFound == false) {
                    for (Map.Entry<Group, List<User>> entry : groupUserMap.entrySet()) {
                        List<User> userList = entry.getValue();
                        Group group = entry.getKey();
                        if (userList.contains(user)) {
                            userList.remove(user);       //removing user from groupUserMap
                            List<Message> messages = groupMessageMap.get(group);
                            for (Message message : senderMap.keySet()) {  //iterating messages in senderMap
                                if (senderMap.get(message) == user) {
                                    messages.remove(message);    //removing message of user from groupMessageMap
                                }
                                senderMap.remove(message);  //removing message of user from senderMap
                            }
                            return groupUserMap.get(group).size() + groupMessageMap.get(group).size() + senderMap.size();
                        }
                    }
                }
                }
            }
            throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int K) throws Exception {
        List<Date> date = new ArrayList<>();
        for(Message message : senderMap.keySet()){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                date.add(message.getTimestamp());
            }
        }
        if(date.size() < K){
            throw new Exception("K is greater than the number of messages");
        }
        else{
            return date.get(K-1).toString();
        }
    }
}
