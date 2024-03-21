package server.websocket;

import java.util.Map;
import org.eclipse.jetty.websocket.api.Session;


public class WebSocketSessions {
    
    private Map<Integer, Map<String, Session>> sessionMap;

    public void addSessionToGame(Integer gameID, String authToken, Session session) {
        if (sessionMap.containsKey(gameID)) {
            sessionMap.get(gameID).put(authToken, session);
        } else {
            sessionMap.put(gameID, Map.of(authToken, session));
        }
    }

    public void removeSessionFromGame(Integer gameID, String authToken, Session session) {
        if (sessionMap.containsKey(gameID)) {
            sessionMap.get(gameID).remove(authToken);
        }
    }

    public void removeSession(Session session) {
        for (Map<String, Session> map : sessionMap.values()) {
            map.values().remove(session);
        }
    }

    public Map<String, Session> getSessionsForGame(Integer gameID) {
        return sessionMap.get(gameID);
    }

}
