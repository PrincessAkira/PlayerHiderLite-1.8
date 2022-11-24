package meow.sarah.patootie;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meow.sarah.patootie.events.PlayerEventHandler;
import meow.sarah.patootie.util.Utils;
import net.labymod.api.LabyModAddon;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.*;
import net.labymod.user.User;
import net.labymod.user.util.UserActionEntry;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

public class Sarah extends LabyModAddon {

    private static Sarah instance;
    public Map<UUID, Integer> playersToRender = new HashMap<>();
    private boolean renderPlayers;
    private boolean modOn;
    private boolean configMessage = true;
    private List<String> playersToRenderString = new ArrayList<>();

    private int key;

    public static Sarah getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        api.registerForgeListener(new PlayerEventHandler());
        api.getEventManager().register(
                (user, entityPlayer, networkPlayerInfo, list) ->
                        list.add(createBlacklistEntry()));
        api.getEventManager().register(
                (user, entityPlayer, networkPlayerInfo, list) ->
                        list.add(createBlacklistRemoval())
        );
    }

    private UserActionEntry createBlacklistEntry() {
        return new UserActionEntry(
                "Blacklist Player",
                UserActionEntry.EnumActionType.NONE,
                null,
                new UserActionEntry.ActionExecutor() {
                    @Override
                    public void execute(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
                        try {
                            UUID uuid = networkPlayerInfo.getGameProfile().getId();
                            playersToRender.put(networkPlayerInfo.getGameProfile().getId(), 0);
                            savePlayersToRender();
                            playersToRenderString.add(networkPlayerInfo.getGameProfile().getName());
                            savePlayersToRenderString();
                            saveConfig();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.sendMessage("Error: " + e.getMessage());
                        }
                    }

                    @Override
                    public boolean canAppear(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
                        return true;
                    }
                }
        );
    }

    private UserActionEntry createBlacklistRemoval() {
        return new UserActionEntry(
                "Remove from Blacklist",
                UserActionEntry.EnumActionType.NONE,
                null,
                new UserActionEntry.ActionExecutor() {
                    @Override
                    public void execute(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
                        try {
                            removePlayer(networkPlayerInfo.getGameProfile().getName());
                            UUID uuid = networkPlayerInfo.getGameProfile().getId();
                            savePlayersToRender();
                            saveConfig();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Utils.sendMessage("Error: " + e.getMessage());
                        }
                    }

                    @Override
                    public boolean canAppear(User user, EntityPlayer entityPlayer, NetworkPlayerInfo networkPlayerInfo) {
                        return true;
                    }
                }
        );
    }

    @Override
    public void loadConfig() {
        JsonObject config = getConfig();
        this.renderPlayers = config.has("renderPlayers") && config.get("renderPlayers").getAsBoolean();
        this.modOn = config.has("enabled") && config.get("enabled").getAsBoolean();
        this.key = config.has("key") ? config.get("key").getAsInt() : -1;
        this.configMessage = config.has("configMessage") && config.get("configMessage").getAsBoolean();
        if (config.has("playersToRenderString")) {
            JsonElement playerArray = config.get("playersToRenderString");
            if (playerArray.isJsonArray()) {
                JsonArray jsonArray = playerArray.getAsJsonArray();
                for (JsonElement jsonElement : jsonArray) {
                    String name = jsonElement.getAsString();
                    playersToRenderString.add(name);
                }
            } else {
                playersToRenderString.add(" ");
            }
        }
        if (config.has("playersToRender")) {
            JsonObject object = config.get("playersToRender").getAsJsonObject();
            Map<UUID, Integer> playersToRender = new HashMap<>();
            for (Map.Entry<String, JsonElement> playerVolumeEntry : object.entrySet()) {
                String id = playerVolumeEntry.getKey();
                int volume = playerVolumeEntry.getValue().getAsInt();
                playersToRender.put(UUID.fromString(id), volume);
            }
            this.playersToRender = playersToRender;
        } else {
            this.playersToRender = new HashMap<>();
        }
    }

    @Override
    protected void fillSettings(List<SettingsElement> subSettings) {
        subSettings.add(new HeaderElement(ModColor.cl('a') + "PlayerHider Settings"));
        subSettings.add(new BooleanElement(
                "Enable PlayerHider",
                this, new ControlElement.IconData(Material.REDSTONE),
                "enabled", modOn)
        );
        subSettings.add(new BooleanElement(
                "Enable Messages",
                this, new ControlElement.IconData(Material.WOOL),
                "configMessage", configMessage)
        );
        KeyElement keyElement = new KeyElement(
                "Key",
                new ControlElement.IconData(Material.REDSTONE_TORCH_ON), this.key, integer -> {
            this.key = integer;
            getConfig().addProperty("key", integer);
        });
        subSettings.add(keyElement);
    }

    public void removePlayer(String s) {
        // remove from the list
        playersToRenderString.remove(s);
        savePlayersToRenderString();
        //  playersToRenderString.removeIf(player -> player.equals(s));
        saveConfig();
    }

    public void savePlayersToRender() {
        JsonObject object = new JsonObject();
        for (Map.Entry<UUID, Integer> uuidIntegerEntry : playersToRender.entrySet()) {
            String uuid = uuidIntegerEntry.getKey().toString();
            Integer volume = uuidIntegerEntry.getValue();
            object.addProperty(uuid, volume);
        }
        getConfig().add("playersToRender", object);
        saveConfig();
    }


    public void savePlayersToRenderString() {
        JsonArray jsonArray = new JsonArray();
        for (String s : playersToRenderString) {
            // check if already in array
            if (!jsonArray.toString().contains(s)) {
                JsonElement jsonElement = new JsonPrimitive(s);
                jsonArray.add(jsonElement);
            }
        }
        getConfig().add("playersToRenderString", jsonArray);
        saveConfig();
    }

    private LabyMod labyMod() {
        return LabyMod.getInstance();
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Map<UUID, Integer> getPlayersToRender() {
        return this.playersToRender;
    }

    public void setPlayersToRender(Map<UUID, Integer> playersToRender) {
        this.playersToRender = playersToRender;
    }

    public boolean isRenderPlayers() {
        return this.renderPlayers;
    }

    public void setRenderPlayers(boolean renderPlayers) {
        this.renderPlayers = renderPlayers;
    }

    public List<String> getPlayersToRenderString() {
        return this.playersToRenderString;
    }

    public List<String> setPlayersToRenderString(List<String> playersToRenderString) {
        return this.playersToRenderString = playersToRenderString;
    }

    public boolean isConfigMessage() {
        return this.configMessage;
    }

    public void setConfigMessage(boolean ConfigMessage) {
        this.configMessage = ConfigMessage;
    }

    public boolean isModOn() {
        return modOn;
    }

    public void setModOn(boolean modOn) {
        this.modOn = modOn;
    }

}