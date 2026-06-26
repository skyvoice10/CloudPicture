package com.example.yupicturebackend.model.vo.space.analyze;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class User {
    int id;
    String info;
    public User(int id,String info){
        this.id=id;
        this.info=info;
    }


    public static  void main(String[] args){
        List<User> userList=new ArrayList<>();
        for(int i=0;i<5;i++){
            User user=new User(i,i+"");
            userList.add(user);
        }
        userList.add(new User(1,"1"));
        userList.add(new User(2,"2"));
        userList.add(new User(2,"2"));
        Map<Integer,List<User>> map=new HashMap<>();
        map=userList.stream().collect(Collectors.groupingBy(User::getId));
        map.entrySet().forEach(i->{
            System.out.println(i.getKey()+":");
            System.out.println(i.getValue());
        });

    }
}
