package me.gravitinos.aigame.common.inventory;

import lombok.Getter;
import me.gravitinos.aigame.common.item.ItemStack;

public class Inventory {

    @Getter
    private int size;

    private ItemStack[] contents;

    public Inventory(int size) {
        this.size = size;
        contents = new ItemStack[size];
    }

    public ItemStack[] getContents() {
        return this.contents;
    }

    public boolean add(ItemStack stack) {
        for(ItemStack stack1 : contents) {
            if(stack1 != null && stack1.getType().equals(stack.getType())) {
                stack1.setAmount(stack1.getAmount() + stack.getAmount());
                return true;
            }
        }
        for(int i = 0; i < contents.length; i++) {
            if(contents[i] == null) {
                contents[i] = stack;
                return true;
            }
        }
        return false;
    }

}
