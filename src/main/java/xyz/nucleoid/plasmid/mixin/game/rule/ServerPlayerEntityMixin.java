package xyz.nucleoid.plasmid.mixin.game.rule;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "stopRiding", at = @At("HEAD"), cancellable = true)
    private void stopRiding(CallbackInfo ci) {
        Entity vehicle = this.getVehicle();
        if (vehicle == null) {
            // how did we get here?
            return;
        }

        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(this.world);
        if (gameSpace != null) {
            if (gameSpace.testRule(GameRule.DISMOUNT_VEHICLE) == RuleResult.DENY) {
                ci.cancel();
                this.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(vehicle));
            }
        }
    }
}
