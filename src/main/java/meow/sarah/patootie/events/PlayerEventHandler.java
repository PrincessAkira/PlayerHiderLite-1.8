package meow.sarah.patootie.events;

import meow.sarah.patootie.Sarah;
import meow.sarah.patootie.util.Utils;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.List;

import static meow.sarah.patootie.util.Utils.SetConfig;
import static meow.sarah.patootie.util.Utils.sendMessage;

public class PlayerEventHandler {

    // im gonna kms ngl
    Sarah instance = Sarah.getInstance();
    LabyMod labymod = LabyMod.getInstance();
    Minecraft minecraft = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onPrePlayerRender(RenderPlayerEvent.Pre e) {
        EntityPlayer enPlayer = e.entityPlayer;
        if (instance.isRenderPlayers() && instance.isModOn()) {
            if (instance.isRenderPlayers() && !enPlayer.equals(minecraft.thePlayer)) {
                List<String> localPlayersToRender = instance.getPlayersToRenderString();
                if (Utils.isNPC(enPlayer)) {
                    e.setCanceled(false);
                    for (String s : localPlayersToRender) {
                        if (s.equals(enPlayer.getGameProfile().getName())) {
                            e.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    public void RemovePlayer(String s) {
        // remove from the list
        instance.getPlayersToRenderString().remove(s);
        instance.savePlayersToRenderString();
        //  playersToRenderString.removeIf(player -> player.equals(s));
        instance.saveConfig();
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent e) {
        EntityPlayer enPlayer = minecraft.thePlayer;
        if (Keyboard.getEventKey() == instance.getKey() && !Keyboard.getEventKeyState() &&
                instance.isModOn() && instance.getKey() > -1) {
            if (instance.isRenderPlayers()) {
                SetConfig(false);
                if (instance.isConfigMessage()) {
                    sendMessage("[PH] - Off");
                }
            } else {
                SetConfig(true);
                if (instance.isConfigMessage()) {
                    sendMessage("[PH] - On");
                }
            }
        } else if (Keyboard.getEventKey() == instance.getKey() &&
                !Keyboard.getEventKeyState() && !instance.isModOn() && instance.getKey() > -1) {
            sendMessage("[PH] - Mod seems to be disabled. Check Config.");
        }
    }
}