package cn.com.guilongkeji;


import com.alibaba.fastjson.JSONObject;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : xgl
 * @date : 2020/4/14 20:45
 * @Description :
 */
@ServerEndpoint("/server/{user}")
public class WebSocketServer {
    private static Map<String,Session> userMap = new HashMap<>();
    private String currentUser;
    @OnOpen
    public void onOpen(Session session, @PathParam("user") String user) throws IOException {
        this.currentUser=user;
        userMap.put(user,session);
        System.out.println(userMap.size());
        for (Session item:userMap.values()) {
            item.getBasicRemote().sendText("系统消息:用户“"+user+"”上线了");
        }
    }
    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws IOException {
        userMap.remove(currentUser);
        for (Session item:userMap.values()) {
            item.getBasicRemote().sendText("系统消息:用户“"+currentUser+"”下线了");
        }
        System.out.println(closeReason.getReasonPhrase());
    }
    @OnError
    public void onError(Throwable error){
        error.printStackTrace();
        System.out.println("通信发生错误"+error.getMessage());
    }
    @OnMessage
    public void onMessage(String msg,Session session) throws IOException {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String data = jsonObject.getString("data");
        if (jsonObject.getInteger("type").equals(0)){
            for (Session item:userMap.values()){
                if (item==session){
                    item.getBasicRemote().sendText("我对所有人说："+data);
                }else {
                    item.getBasicRemote().sendText(currentUser+"对所有人说："+data);
                }
            }
        }else {
            String user = jsonObject.getString("user");
            if (userMap.containsKey(user)){
                userMap.get(user).getBasicRemote().sendText(currentUser+"对你说："+data);
                session.getBasicRemote().sendText("你对"+user+"说："+data);
            }else {
                session.getBasicRemote().sendText("系统消息：未查询到"+user+"用户");
            }
        }
    }
}
