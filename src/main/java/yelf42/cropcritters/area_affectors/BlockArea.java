package yelf42.cropcritters.area_affectors;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public record BlockArea(BlockPos position, int horizontalRange, int verticalRange, ShapeType shape) {
    private static final Map<BlockArea, List<ChunkSectionPos>> BLOCK_AREA_SECTIONS = new ConcurrentHashMap<>();
    public static final MapCodec<BlockArea> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(BlockPos.CODEC.fieldOf(
                    "position").forGetter(BlockArea::position),
            Codecs.POSITIVE_INT.fieldOf("horizontal_range").forGetter(BlockArea::horizontalRange),
            Codecs.POSITIVE_INT.fieldOf("vertical_range").forGetter(BlockArea::verticalRange),
            ShapeType.CODEC.fieldOf("shape").forGetter(BlockArea::shape)).apply(instance, BlockArea::new));

    BlockArea(AffectorType type, BlockPos position) {
        this(position,
                type.width,
                type.height,
                type.shape);
    }

    public boolean isPositionInside(BlockPos blockPos) {
        return this.shape().isPositionInside(this.position(), blockPos, this.horizontalRange(), this.verticalRange());
    }

    public void getAllSections(Consumer<ChunkSectionPos> sectionConsumer) {
        BLOCK_AREA_SECTIONS.computeIfAbsent(this.asKey(), (BlockArea blockArea) -> {
            ImmutableList.Builder<@NotNull ChunkSectionPos> builder = ImmutableList.builder();
            getAllSections(this.position(), this.horizontalRange(), this.verticalRange(), this.shape(), builder::add);
            return builder.build();
        }).forEach(sectionConsumer);
    }

    private BlockArea asKey() {
        // we only allow for chunk intervals as range in the config, which allows us to simplify the position here to the section positions
        return new BlockArea(ChunkSectionPos.from(this.position()).getMinPos(),
                this.horizontalRange(),
                this.verticalRange(),
                this.shape());
    }

    public static void getAllSections(BlockPos blockPos, int horizontalRange, int verticalRange, ShapeType shapeType, Consumer<ChunkSectionPos> ChunkSectionPosConsumer) {
        ChunkSectionPos minSection = ChunkSectionPos.from(blockPos.add(-horizontalRange, -verticalRange, -horizontalRange));
        ChunkSectionPos maxSection = ChunkSectionPos.from(blockPos.add(horizontalRange, verticalRange, horizontalRange));
        for (int sectionX = minSection.getSectionX(); sectionX <= maxSection.getSectionX(); sectionX++) {
            int posX = getClosestCoordinate(blockPos.getX(), sectionX);
            for (int sectionY = minSection.getSectionY(); sectionY <= maxSection.getSectionY(); sectionY++) {
                int posY = getClosestCoordinate(blockPos.getY(), sectionY);
                for (int sectionZ = minSection.getSectionZ(); sectionZ <= maxSection.getSectionZ(); sectionZ++) {
                    int posZ = getClosestCoordinate(blockPos.getZ(), sectionZ);
                    if (shapeType.isPositionInside(blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ(),
                            posX,
                            posY,
                            posZ,
                            horizontalRange,
                            verticalRange)) {
                        ChunkSectionPosConsumer.accept(ChunkSectionPos.from(sectionX, sectionY, sectionZ));
                    }
                }
            }
        }
    }

    private static int getClosestCoordinate(int blockCoordinate, int sectionCoordinate) {
        int min = ChunkSectionPos.getBlockCoord(sectionCoordinate);
        if (blockCoordinate < min) {
            return min;
        }

        int max = ChunkSectionPos.getOffsetPos(sectionCoordinate, 15);
        if (blockCoordinate > max) {
            return max;
        }

        return blockCoordinate;
    }
}