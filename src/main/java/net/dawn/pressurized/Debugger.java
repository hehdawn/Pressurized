package net.dawn.pressurized;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

//cool debugging contents for the mod buh

public class Debugger {
    public static void renderLineBox(PoseStack poseStack, VertexConsumer consumer, AABB box) {
        box = VSCompat.ShipyardAirpocketToWorld(Minecraft.getInstance().level, box);

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;

        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        float red = 1.0F;
        float green = 0.0F;
        float blue = 0.0F;
        float alpha = 1.0F;

        PoseStack.Pose pose = poseStack.last();

        drawLine(pose, consumer, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        drawLine(pose, consumer, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
        drawLine(pose, consumer, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
        drawLine(pose, consumer, minX, minY, maxZ, minX, minY, minZ, red, green, blue, alpha);

        drawLine(pose, consumer, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        drawLine(pose, consumer, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        drawLine(pose, consumer, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        drawLine(pose, consumer, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);

        drawLine(pose, consumer, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        drawLine(pose, consumer, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        drawLine(pose, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
        drawLine(pose, consumer, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
    }

    private static void drawLine(PoseStack.Pose pose, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
        Vector3f normal = new Vector3f(0.0F, 1.0F, 0.0F);

        consumer.vertex(pose.pose(), x1, y1, z1)
                .color(red, green, blue, alpha)
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();

        consumer.vertex(pose.pose(), x2, y2, z2)
                .color(red, green, blue, alpha)
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();
    }
}
