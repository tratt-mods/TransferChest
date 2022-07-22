package net.transferchest.mod.gui.handler;

import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WPlayerInvPanel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.LiteralTextContent;
import net.transferchest.mod.core.TransferChestHandler;
import net.transferchest.mod.entity.TransferChestBlockEntity;
import net.transferchest.mod.gui.widget.WReleaseFocusListPanel;
import net.transferchest.mod.initializer.TCGUIHandlers;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class TransferChestGUIHandler extends SyncedGuiDescription
{
    private TransferChestBlockEntity bufferEntity;
    private ArrayList<String> watchersName;
    private WReleaseFocusListPanel namesPanel;
    
    public TransferChestGUIHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context)
    {
        super(TCGUIHandlers.TRANSFER_CHEST_GUI_HANDLER, syncId, playerInventory, getBlockInventory(context, TransferChestBlockEntity.INVENTORY_SIZE), null);
        watchersName = new ArrayList<>();
        initializeBufferEntity(context);
        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        for(int i = 0; i < blockInventory.size(); i++)
        {
            WItemSlot current = WItemSlot.of(blockInventory, i);
            if(i < blockInventory.size() / 2)
            {
                root.add(current, i * 18, 20);
            }
            else
            {
                root.add(current, (i - (blockInventory.size() / 2)) * 18, 38);
            }
        }
        
        BiConsumer<String, PlayerNameEntry> configurator = (String s, PlayerNameEntry playerNameEntry) ->
        {
            playerNameEntry.name.setText(new LiteralTextContent(s));
        };
        namesPanel = new WReleaseFocusListPanel(watchersName, PlayerNameEntry::new, configurator);
        namesPanel.setListItemHeight(12);
        root.add(namesPanel, 92, 2, 70, 70);
        WPlayerInvPanel playerInvPanel = this.createPlayerInventoryPanel();
        root.add(playerInvPanel, 2, 70);
        root.validate(this);
        blockInventory.onOpen(playerInventory.player);
    }
    
    private void initializeBufferEntity(ScreenHandlerContext context)
    {
        TransferChestBlockEntity[] lambdaBypass = new TransferChestBlockEntity[1];
        
        context.run((world, blockPosition) ->
        {
            TransferChestBlockEntity temporaryEntity = (TransferChestBlockEntity) world.getBlockEntity(blockPosition);
            lambdaBypass[0] = temporaryEntity;
        });
        bufferEntity = lambdaBypass[0];
    }
    
    public void updatePanelEntries(String[] names)
    {
        watchersName.clear();
        for(String name : names)
        {
            name = "12345678910";
            if(name.length() > 10)
            {
                name = name.substring(0, 8);
                name += "...";
            }
            watchersName.add(name);
        }
        namesPanel.layout();
    }
    
    
    public String getOwnerName()
    {
        return playerInventory.player.getName().getContent();
    }
    
    @Override public void close(PlayerEntity player)
    {
        if(!world.isClient)
        {
            TransferChestHandler.closeGUI(world, this);
            blockInventory.onClose(playerInventory.player);
        }
        
        super.close(player);
    }
    
    public static class PlayerNameEntry extends WPlainPanel
    {
        WLabel name;
        
        public PlayerNameEntry()
        {
            name = new WLabel("Placeholder");
            this.add(name, 0, 0);
            this.setSize(3 * 18, 12);
        }
    }
}
