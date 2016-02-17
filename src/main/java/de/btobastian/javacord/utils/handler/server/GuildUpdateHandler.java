/*
 * Copyright (C) 2016 Bastian Oppermann
 * 
 * This file is part of Javacord.
 * 
 * Javacord is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser general Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Javacord is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.btobastian.javacord.utils.handler.server;

import de.btobastian.javacord.ImplDiscordAPI;
import de.btobastian.javacord.entities.impl.ImplServer;
import de.btobastian.javacord.listener.Listener;
import de.btobastian.javacord.listener.server.ServerChangeNameListener;
import de.btobastian.javacord.utils.PacketHandler;
import org.json.JSONObject;

import java.util.List;

/**
 * Handles the guild update packet.
 */
public class GuildUpdateHandler extends PacketHandler {

    /**
     * Creates a new instance of this class.
     *
     * @param api The api.
     */
    public GuildUpdateHandler(ImplDiscordAPI api) {
        super(api, true, "GUILD_UPDATE");
    }

    @Override
    public void handle(JSONObject packet) {
        if (packet.has("unavailable") && packet.getBoolean("unavailable")) {
            return;
        }
        final ImplServer server = (ImplServer) api.getServerById(packet.getString("id"));

        String name = packet.getString("name");
        if (!server.getName().equals(name)) {
            final String oldName = server.getName();
            server.setName(name);
            listenerExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    List<Listener> listeners =  api.getListeners(ServerChangeNameListener.class);
                    synchronized (listeners) {
                        for (Listener listener : listeners) {
                            ((ServerChangeNameListener) listener).onServerChangeName(api, server, oldName);
                        }
                    }
                }
            });
        }
    }

}
