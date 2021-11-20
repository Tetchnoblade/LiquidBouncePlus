/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.event.TickEvent;

import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

import net.ccbluex.liquidbounce.utils.timer.MSTimer;

import java.util.ArrayList;

public class PacketUtils extends MinecraftInstance implements Listenable {

    private static int inBound, outBound = 0;
    public static int avgInBound, avgOutBound = 0;

    private static ArrayList<Packet<INetHandlerPlayServer>> packets = new ArrayList<Packet<INetHandlerPlayServer>>();

    private static MSTimer packetTimer, wdTimer = new MSTimer();

    private static int transCount = 0;
    private static int wdVL = 0;

    private static boolean isInventoryAction(short action) {
        return action > 0 && action < 100;
    }

    public static boolean isWatchdogActive() {
        return wdVL >= 8;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket().getClass().getSimpleName().startsWith("C")) outBound++;
        else if (event.getPacket().getClass().getSimpleName().startsWith("S")) inBound++;

        if (event.getPacket() instanceof C0FPacketConfirmTransaction) 
        {
            if (!isInventoryAction(((C0FPacketConfirmTransaction) event.getPacket()).uid)) 
                transCount++;
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (packetTimer.hasTimePassed(1000L)) {
            avgInBound = inBound; avgOutBound = outBound;
            inBound = outBound = 0;
            packetTimer.reset();
        }
        if (!ServerUtils.isOnHypixel()) {
            // reset all vl
            wdVL = transCount = 0;
            wdTimer.reset();
        } else {
            if (wdTimer.hasTimePassed(100L)) {
                wdVL += (transCount > 1) ? 1 : -1;
                transCount = 0;
                if (wdVL > 10) wdVL = 10;
                if (wdVL < 0) wdVL = 0;
                wdTimer.reset();
            }
        }
    }

    /*
     * This code is from UnlegitMC/FDPClient. Please credit them when using this code in your repository.
     */
    public static void sendPacketNoEvent(Packet<INetHandlerPlayServer> packet) {
        packets.add(packet);
        mc.getNetHandler().addToSendQueue(packet);
    }

    public static boolean handleSendPacket(Packet<?> packet) {
        if (packets.contains(packet)) {
            packets.remove(packet);
            return true;
        }
        return false;
    }

    /**
     * @return wow
     */
    @Override
    public boolean handleEvents() {
        return true;
    }
    
}