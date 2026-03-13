package dev.jab125.drm.mixin;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.ingame.InGameInputHandler;
import dev.isxander.controlify.virtualmouse.VirtualMouseHandler;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@org.spongepowered.asm.mixin.Mixin(dev.isxander.controlify.Controlify.class)
public interface ControlifyAccessor {
	@Accessor
	static Controlify getInstance() {
		throw new UnsupportedOperationException();
	}

	@Accessor
	static void setInstance(Controlify instance) {
		throw new UnsupportedOperationException();
	}

	@Invoker
	void callTickActiveController(ControllerEntity controller, boolean outOfFocus);

	@Accessor
	int getConsecutiveInputSwitches();

	@Accessor
	void setConsecutiveInputSwitches(int consecutiveInputSwitches);

	@Accessor
	InGameInputHandler getInGameInputHandler();

	@Accessor
	void setInGameInputHandler(InGameInputHandler inGameInputHandler);

	@Accessor
	VirtualMouseHandler getVirtualMouseHandler();

	@Accessor
	void setVirtualMouseHandler(VirtualMouseHandler virtualMouseHandler);

	@Accessor
	ControllerEntity getCurrentController();

	@Accessor
	void setCurrentController(ControllerEntity currentController);

	@Accessor
	InputMode getCurrentInputMode();

	@Accessor
	void setCurrentInputMode(InputMode currentInputMode);
}
