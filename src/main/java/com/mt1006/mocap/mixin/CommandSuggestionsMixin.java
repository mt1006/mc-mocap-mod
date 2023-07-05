package com.mt1006.mocap.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mt1006.mocap.command.InputArgument;
import net.minecraft.client.gui.CommandSuggestionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(CommandSuggestionHelper.class)
public class CommandSuggestionsMixin
{
	@Redirect(method = "updateCommandInfo", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;getCompletionSuggestions(Lcom/mojang/brigadier/ParseResults;I)Ljava/util/concurrent/CompletableFuture;"))
	private <S> CompletableFuture<Suggestions> atListSuggestions(CommandDispatcher<S> dispatcher, final ParseResults<S> parse, int cursor)
	{
		CompletableFuture<Suggestions> suggestions = InputArgument.getSuggestions(parse.getContext(), parse.getReader().getString(), cursor);
		return suggestions != null ? suggestions : dispatcher.getCompletionSuggestions(parse, cursor);
	}
}
