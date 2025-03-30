package io.github.bl3rune.blu3printPlugin.data;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class MaterialData {

    private String name;
    private Material material;
    private BlockFace face;
    private int count;

    public MaterialData(String name, Material material, BlockFace face, int count) {
        this.name = name;
        this.material = material;
        this.face = face;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public BlockFace getFace()  {
        return face;
    }

    public int getCount()  {
        return count;
    }

    public void setName(String name)  {
        this.name = name;
    }

    public void setMaterial(Material material)  {
        this.material = material;
    }

    public void setFace(BlockFace face)  {
        this.face = face;
    }

    public void setCount(int count)   {
        this.count = count;
    }

}
