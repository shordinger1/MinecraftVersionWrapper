package shordinger.ModWrapper.migration.wrapper.minecraft.block.state;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.Block;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.material.EnumPushReaction;
import shordinger.ModWrapper.migration.wrapper.minecraft.block.properties.IProperty;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.EnumBlockRenderType;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.MapPopulator;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.Rotation;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.AxisAlignedBB;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.BlockPos;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Cartesian;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.RayTraceResult;
import shordinger.ModWrapper.migration.wrapper.minecraft.util.math.Vec3d;
import shordinger.ModWrapper.migration.wrapper.minecraft.world.IWrapperBlockAccess;

import static net.minecraft.block.Block.blockRegistry;

public class BlockStateContainer {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private static final Function<IProperty<?>, String> GET_NAME_FUNC = new Function<IProperty<?>, String>() {

        @Nullable
        public String apply(@Nullable IProperty<?> p_apply_1_) {
            return p_apply_1_ == null ? "<NULL>" : p_apply_1_.getName();
        }
    };
    private final net.minecraft.block.Block block;
    private final ImmutableSortedMap<String, IProperty<?>> properties;
    private final ImmutableList<IBlockState> validStates;

    public BlockStateContainer(net.minecraft.block.Block blockIn, IProperty<?>... properties) {
        this(blockIn, properties, null);
    }

    protected StateImplementation createState(net.minecraft.block.Block block, ImmutableMap<IProperty<?>, Comparable<?>> properties,
                                              @Nullable ImmutableMap<net.minecraftforge.common.property.IUnlistedProperty<?>, java.util.Optional<?>> unlistedProperties) {
        return new StateImplementation(block, properties);
    }

    protected BlockStateContainer(net.minecraft.block.Block blockIn, IProperty<?>[] properties,
                                  ImmutableMap<net.minecraftforge.common.property.IUnlistedProperty<?>, java.util.Optional<?>> unlistedProperties) {
        this.block = blockIn;
        Map<String, IProperty<?>> map = Maps.<String, IProperty<?>>newHashMap();

        for (IProperty<?> iproperty : properties) {
            validateProperty(blockIn, iproperty);
            map.put(iproperty.getName(), iproperty);
        }

        this.properties = ImmutableSortedMap.copyOf(map);
        Map<Map<IProperty<?>, Comparable<?>>, BlockStateContainer.StateImplementation> map2 = Maps.<Map<IProperty<?>, Comparable<?>>, BlockStateContainer.StateImplementation>newLinkedHashMap();
        List<BlockStateContainer.StateImplementation> list1 = Lists.<BlockStateContainer.StateImplementation>newArrayList();

        for (List<Comparable<?>> list : Cartesian.cartesianProduct(this.getAllowedValues())) {
            Map<IProperty<?>, Comparable<?>> map1 = MapPopulator
                .createMap(this.properties.values(), list);
            BlockStateContainer.StateImplementation blockstatecontainer$stateimplementation = createState(
                blockIn,
                ImmutableMap.copyOf(map1),
                unlistedProperties);
            map2.put(map1, blockstatecontainer$stateimplementation);
            list1.add(blockstatecontainer$stateimplementation);
        }

        for (BlockStateContainer.StateImplementation blockstatecontainer$stateimplementation1 : list1) {
            blockstatecontainer$stateimplementation1.buildPropertyValueTable(map2);
        }

        this.validStates = ImmutableList.<IBlockState>copyOf(list1);
    }

    public static <T extends Comparable<T>> String validateProperty(net.minecraft.block.Block block, IProperty<T> property) {
        String s = property.getName();

        if (!NAME_PATTERN.matcher(s)
            .matches()) {
            throw new IllegalArgumentException("Block: " + block.getClass() + " has invalidly named property: " + s);
        } else {
            for (T t : property.getAllowedValues()) {
                String s1 = property.getName(t);

                if (!NAME_PATTERN.matcher(s1)
                    .matches()) {
                    throw new IllegalArgumentException(
                        "Block: " + block.getClass() + " has property: " + s + " with invalidly named value: " + s1);
                }
            }

            return s;
        }
    }

    public ImmutableList<IBlockState> getValidStates() {
        return this.validStates;
    }

    private List<Iterable<Comparable<?>>> getAllowedValues() {
        List<Iterable<Comparable<?>>> list = Lists.<Iterable<Comparable<?>>>newArrayList();
        ImmutableCollection<IProperty<?>> immutablecollection = this.properties.values();

        for (IProperty<?> iProperty : immutablecollection) {
            list.add(((IProperty) iProperty).getAllowedValues());
        }

        return list;
    }

    public IBlockState getBaseState() {
        return (IBlockState) this.validStates.get(0);
    }

    public net.minecraft.block.Block getBlock() {
        return this.block;
    }

    public Collection<IProperty<?>> getProperties() {
        return this.properties.values();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("block", blockRegistry.getNameForObject(this.block))
            .add("properties", Iterables.transform(this.properties.values(), GET_NAME_FUNC))
            .toString();
    }

    @Nullable
    public IProperty<?> getProperty(String propertyName) {
        return (IProperty) this.properties.get(propertyName);
    }

    public static class StateImplementation extends BlockStateBase {

        private final Block block;
        private final ImmutableMap<IProperty<?>, Comparable<?>> properties;
        protected ImmutableTable<IProperty<?>, Comparable<?>, IBlockState> propertyValueTable;

        protected StateImplementation(net.minecraft.block.Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn) {
            this.block = (Block) blockIn;
            this.properties = propertiesIn;
        }

        protected StateImplementation(net.minecraft.block.Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn,
                                      ImmutableTable<IProperty<?>, Comparable<?>, IBlockState> propertyValueTable) {
            this.block = (Block) blockIn;
            this.properties = propertiesIn;
            this.propertyValueTable = propertyValueTable;
        }

        public Collection<IProperty<?>> getPropertyKeys() {
            return Collections.<IProperty<?>>unmodifiableCollection(this.properties.keySet());
        }

        /**
         * Get the value of the given Property for this BlockState
         */
        public <T extends Comparable<T>> T getValue(IProperty<T> property) {
            Comparable<?> comparable =  this.properties.get(property);

            if (comparable == null) {
                throw new IllegalArgumentException(
                    "Cannot get property " + property + " as it does not exist in " + this.block.getBlockState());
            } else {
                return (T) (property.getValueClass()
                    .cast(comparable));
            }
        }

        /**
         * Get a version of this BlockState with the given Property now set to the given value
         */
        public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
            Comparable<?> comparable = this.properties.get(property);

            if (comparable == null) {
                throw new IllegalArgumentException(
                    "Cannot set property " + property + " as it does not exist in " + this.block.getBlockState());
            } else if (comparable == value) {
                return this;
            } else {
                IBlockState iblockstate = (IBlockState) this.propertyValueTable.get(property, value);

                if (iblockstate == null) {
                    throw new IllegalArgumentException(
                        "Cannot set property " + property
                            + " to "
                            + value
                            + " on block "
                            + net.minecraft.block.Block.blockRegistry.getNameForObject(this.block)
                            + ", it is not an allowed value");
                } else {
                    return iblockstate;
                }
            }
        }

        public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {
            return this.properties;
        }

        public Block getBlock() {
            return this.block;
        }


        public int hashCode() {
            return this.properties.hashCode();
        }

        public void buildPropertyValueTable(
            Map<Map<IProperty<?>, Comparable<?>>, BlockStateContainer.StateImplementation> map) {
            if (this.propertyValueTable != null) {
                throw new IllegalStateException();
            } else {
                Table<IProperty<?>, Comparable<?>, IBlockState> table = HashBasedTable
                    .<IProperty<?>, Comparable<?>, IBlockState>create();

                for (Entry<IProperty<?>, Comparable<?>> iPropertyComparableEntry : this.properties.entrySet()) {
                    IProperty<?> iproperty = ((Entry<IProperty<?>, Comparable<?>>) (Entry) iPropertyComparableEntry).getKey();

                    for (Comparable<?> comparable : iproperty.getAllowedValues()) {
                        if (comparable != ((Entry<IProperty<?>, Comparable<?>>) (Entry) iPropertyComparableEntry).getValue()) {
                            table.put(
                                iproperty,
                                comparable,
                                map.get(this.getPropertiesWithValue(iproperty, comparable)));
                        }
                    }
                }

                this.propertyValueTable = ImmutableTable.copyOf(table);
            }
        }

        private Map<IProperty<?>, Comparable<?>> getPropertiesWithValue(IProperty<?> property, Comparable<?> value) {
            Map<IProperty<?>, Comparable<?>> map = Maps.<IProperty<?>, Comparable<?>>newHashMap(this.properties);
            map.put(property, value);
            return map;
        }

        public Material getMaterial() {
            return this.block.getMaterial(this);
        }

        public boolean isFullBlock() {
            return this.block.isFullBlock(this);
        }

        public boolean canEntitySpawn(Entity entityIn) {
            return this.block.canEntitySpawn(this, entityIn);
        }

        public int getLightOpacity() {
            return this.block.getLightOpacity(this);
        }

        public int getLightValue() {
            return this.block.getLightValue(this);
        }

        @SideOnly(Side.CLIENT)
        public boolean isTranslucent() {
            return this.block.isTranslucent(this);
        }

        public boolean useNeighborBrightness() {
            return this.block.getUseNeighborBrightness(this);
        }

        public MapColor getMapColor(IWrapperBlockAccess p_185909_1_, BlockPos p_185909_2_) {
            return this.block.getMapColor(this, p_185909_1_, p_185909_2_);
        }

        /**
         * Returns the blockstate with the given rotation. If inapplicable, returns itself.
         */
        public IBlockState withRotation(Rotation rot) {
            return this.block.withRotation(this, rot);
        }

        /**
         * Returns the blockstate mirrored in the given way. If inapplicable, returns itself.
         */
        public IBlockState withMirror(Mirror mirrorIn) {
            return this.block.withMirror(this, mirrorIn);
        }

        public boolean isFullCube() {
            return this.block.isFullCube(this);
        }

        @SideOnly(Side.CLIENT)
        public boolean hasCustomBreakingProgress() {
            return this.block.hasCustomBreakingProgress(this);
        }

        public EnumBlockRenderType getRenderType() {
            return this.block.getRenderType(this);
        }

        @SideOnly(Side.CLIENT)
        public int getPackedLightmapCoords(IWrapperBlockAccess source, BlockPos pos) {
            return this.block.getPackedLightmapCoords(this, source, pos);
        }

        @SideOnly(Side.CLIENT)
        public float getAmbientOcclusionLightValue() {
            return this.block.getAmbientOcclusionLightValue(this);
        }

        public boolean isBlockNormalCube() {
            return this.block.isBlockNormalCube(this);
        }

        public boolean isNormalCube() {
            return this.block.isNormalCube(this);
        }

        public boolean canProvidePower() {
            return this.block.canProvidePower(this);
        }

        public int getWeakPower(IWrapperBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
            return this.block.getWeakPower(this, blockAccess, pos, side);
        }

        public boolean hasComparatorInputOverride() {
            return this.block.hasComparatorInputOverride(this);
        }

        public int getComparatorInputOverride(World worldIn, BlockPos pos) {
            return this.block.getComparatorInputOverride(this, worldIn, pos);
        }

        public float getBlockHardness(World worldIn, BlockPos pos) {
            return this.block.getBlockHardness(this, worldIn, pos);
        }

        public float getPlayerRelativeBlockHardness(EntityPlayer player, World worldIn, BlockPos pos) {
            return this.block.getPlayerRelativeBlockHardness(this, player, worldIn, pos);
        }

        public int getStrongPower(IWrapperBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
            return this.block.getStrongPower(this, blockAccess, pos, side);
        }

        public EnumPushReaction getMobilityFlag() {
            return this.block.getMobilityFlag(this);
        }

        public IBlockState getActualState(IWrapperBlockAccess blockAccess, BlockPos pos) {
            return this.block.getActualState(this, blockAccess, pos);
        }

        @SideOnly(Side.CLIENT)
        public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
            return this.block.getSelectedBoundingBox(this, worldIn, pos);
        }

        @SideOnly(Side.CLIENT)
        public boolean shouldSideBeRendered(IWrapperBlockAccess blockAccess, BlockPos pos, EnumFacing facing) {
            return this.block.shouldSideBeRendered(this, blockAccess, pos, facing);
        }

        public boolean isOpaqueCube() {
            return this.block.isOpaqueCube(this);
        }

        @Nullable
        public AxisAlignedBB getCollisionBoundingBox(IWrapperBlockAccess worldIn, BlockPos pos) {
            return this.block.getCollisionBoundingBox(this, worldIn, pos);
        }

        public void addCollisionBoxToList(World worldIn, BlockPos pos, AxisAlignedBB entityBox,
                                          List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185908_6_) {
            this.block.addCollisionBoxToList(this, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185908_6_);
        }

        public AxisAlignedBB getBoundingBox(IWrapperBlockAccess blockAccess, BlockPos pos) {
            return this.block.getBoundingBox(this, blockAccess, pos);
        }

        public RayTraceResult collisionRayTrace(World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
            return this.block.collisionRayTrace(this, worldIn, pos, start, end);
        }

        /**
         * Determines if the block is solid enough on the top side to support other blocks, like redstone
         * components.
         */
        public boolean isTopSolid() {
            return this.block.isTopSolid(this);
        }

        public Vec3d getOffset(IWrapperBlockAccess access, BlockPos pos) {
            return this.block.getOffset(this, access, pos);
        }

        /**
         * Called on both Client and Server when World#addBlockEvent is called. On the Server, this may perform
         * additional changes to the world, like pistons replacing the block with an extended base. On the client,
         * the update may involve replacing tile entities, playing sounds, or performing other visual actions to
         * reflect the server side changes.
         */
        public boolean onBlockEventReceived(World worldIn, BlockPos pos, int id, int param) {
            return this.block.eventReceived(this, worldIn, pos, id, param);
        }

        /**
         * Called when a neighboring block was changed and marks that this state should perform any checks during a
         * neighbor change. Cases may include when redstone power is updated, cactus blocks popping off due to a
         * neighboring solid block, etc.
         *
         * @param blockIn The neighboring block causing this block update
         * @param fromPos The neighboring position causing this block update
         */
        public void neighborChanged(World worldIn, BlockPos pos, net.minecraft.block.Block blockIn, BlockPos fromPos) {
            this.block.neighborChanged(this, worldIn, pos, (Block) blockIn, fromPos);
        }

        public boolean causesSuffocation() {
            return this.block.causesSuffocation(this);
        }

        public BlockFaceShape getBlockFaceShape(IWrapperBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
            return this.block.getBlockFaceShape(worldIn, this, pos, facing);
        }

        // Forge Start
        @Override
        public ImmutableTable<IProperty<?>, Comparable<?>, IBlockState> getPropertyValueTable() {
            return propertyValueTable;
        }

        @Override
        public int getLightOpacity(IWrapperBlockAccess world, BlockPos pos) {
            return this.block.getLightOpacity(this, world, pos);
        }

        @Override
        public int getLightValue(IWrapperBlockAccess world, BlockPos pos) {
            return this.block.getLightValue(this, world, pos);
        }

        @Override
        public boolean isSideSolid(IWrapperBlockAccess world, BlockPos pos, EnumFacing side) {
            return this.block.isSideSolid(this, world, pos, side);
        }

        @Override
        public boolean doesSideBlockChestOpening(IWrapperBlockAccess world, BlockPos pos, EnumFacing side) {
            return this.block.doesSideBlockChestOpening(this, world, pos, side);
        }

        @Override
        public boolean doesSideBlockRendering(IWrapperBlockAccess world, BlockPos pos, EnumFacing side) {
            return this.block.doesSideBlockRendering(this, world, pos, side);
        }
    }

    /**
     * Forge added class to make building things easier.
     * Will return an instance of BlockStateContainer appropriate for
     * the list of properties passed in.
     *
     * Example usage:
     *
     * protected BlockStateContainer createBlockState()
     * {
     * return (new BlockStateContainer.Builder(this)).add(FACING).add(SOME_UNLISTED).build();
     * }
     *
     */
    public static class Builder {

        private final net.minecraft.block.Block block;
        private final List<IProperty<?>> listed = Lists.newArrayList();
        private final List<net.minecraftforge.common.property.IUnlistedProperty<?>> unlisted = Lists.newArrayList();

        public Builder(net.minecraft.block.Block block) {
            this.block = block;
        }

        public Builder add(IProperty<?>... props) {
            Collections.addAll(this.listed, props);
            return this;
        }

        public Builder add(net.minecraftforge.common.property.IUnlistedProperty<?>... props) {
            Collections.addAll(this.unlisted, props);
            return this;
        }

        public BlockStateContainer build() {
            IProperty<?>[] listed = new IProperty[this.listed.size()];
            listed = this.listed.toArray(listed);
            if (this.unlisted.size() == 0) return new BlockStateContainer(this.block, listed);

            net.minecraftforge.common.property.IUnlistedProperty<?>[] unlisted = new net.minecraftforge.common.property.IUnlistedProperty[this.unlisted
                .size()];
            unlisted = this.unlisted.toArray(unlisted);

            return new net.minecraftforge.common.property.ExtendedBlockState(this.block, listed, unlisted);
        }
    }
}
