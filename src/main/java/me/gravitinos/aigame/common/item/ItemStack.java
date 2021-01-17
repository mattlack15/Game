package me.gravitinos.aigame.common.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ItemStack {
    private Material type;
    private int amount;
}
