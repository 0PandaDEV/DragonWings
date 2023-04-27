package tk.pandadev.dragonwings;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.math.Quaternion;

public class DragonWings implements ClientModInitializer {
    private static final Identifier DRAGON_WINGS_TEXTURE = new Identifier("mymod", "textures/entity/dragon_wings.png");
    private static NativeImageBackedTexture DRAGON_WINGS_TEXTURE_BACKED;

    @Override
    public void onInitializeClient() {
        // Load the dragon wings texture asynchronously
        CompletableFuture.runAsync(() -> {
            try (InputStream inputStream = MinecraftClient.getInstance().getResourceManager().getResource(DRAGON_WINGS_TEXTURE).get().getInputStream()) {
                // Read the contents of the input stream into a byte array
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                byte[] data = outputStream.toByteArray();

                NativeImage image = NativeImage.read(new ByteArrayInputStream(data));

                DRAGON_WINGS_TEXTURE_BACKED = new NativeImageBackedTexture(image);
                MinecraftClient.getInstance().execute(() -> {
                    // Register the dragon wings texture with the texture manager
                    MinecraftClient.getInstance().getTextureManager().registerTexture(DRAGON_WINGS_TEXTURE, DRAGON_WINGS_TEXTURE_BACKED);

                    // Register a new entity renderer for the player that renders the dragon wings on their back
                    EntityRendererRegistry.register(EntityType.PLAYER, (EntityRendererFactory.Context ctx) -> new PlayerEntityRenderer(ctx, true) {
                        public void render(PlayerEntity player, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
                            // Render the dragon wings on the player's back
                            renderDragonWings(matrices, vertexConsumers, light, player, DRAGON_WINGS_TEXTURE_BACKED.getGlId(), 1);

                            super.render((AbstractClientPlayerEntity) player, yaw, tickDelta, matrices, vertexConsumers, light);
                        }
                    });
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void renderDragonWings(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, int textureId, float tickDelta) {
        double x = 0.125;
        double y = 0.125;
        double z = 0.125;
        double width = 0.75;
        double height = 0.5;

        matrices.push();
        matrices.translate(0.0, 0.5 * (entity.getHeight() + height), 0.1 * entity.getWidth());

        Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
        quaternion.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(90.0F));
        matrices.multiply(quaternion);

        // Get the player's eye position
        Vec3d eyePos = entity.getCameraPosVec(tickDelta);

        // Calculate the position of the wings relative to the player's position
        double wingX = eyePos.getX() - entity.getX();
        double wingY = eyePos.getY() - entity.getY();
        double wingZ = eyePos.getZ() - entity.getZ();

        // Translate the matrix stack to the position of the wings
        matrices.translate(wingX, wingY, wingZ);

        // Rotate the wings according to the player's rotation
        quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
        float yaw = MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) * 0.017453292F;
        float pitch = MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch()) * 0.017453292F;
        quaternion.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-yaw));
        quaternion.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-pitch));
        matrices.multiply(quaternion);

        // Translate the matrix stack to the center of the wings
        matrices.translate(-x - width / 2.0, -y, -z);

        // Render the wings using the provided texture ID
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(textureId));
        vertexConsumer.vertex(matrices.peek().getModel(), 0.0f, 0.0f, 0.0f).color(255, 255, 255, 255).texture(0.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrices.peek().getNormal(), 0.0f, 1.0f, 0.0f).next();
        vertexConsumer.vertex(matrices.peek().getModel(), 0.0f, (float) height, 0.0f).color(255, 255, 255, 255).texture(0.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrices.peek().getNormal(), 0.0f, 1.0f, 0.0f).next();
        vertexConsumer.vertex(matrices.peek().getModel(), (float) width, (float) height, 0.0f).color(255, 255, 255, 255).texture(1.0f, 1.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrices.peek().getNormal(), 0.0f, 1.0f, 0.0f).next();
        vertexConsumer.vertex(matrices.peek().getModel(), (float) width, 0.0f, 0.0f).color(255, 255, 255, 255).texture(1.0f, 0.0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(matrices.peek().getNormal(), 0.0f, 1.0f, 0.0f).next();

        matrices.pop();
    }
}