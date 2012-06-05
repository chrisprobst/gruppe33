package propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid;

import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import com.foxnet.rmi.pattern.change.Session;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.GraphicsEntity;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.Scene;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.SceneProcessor;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.math.Vector2f.Direction;

public class InputUploader extends GraphicsEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final UUID peerKey;
	private final Map<Direction, Boolean> inputMap = new EnumMap<Direction, Boolean>(
			Direction.class);

	public InputUploader(UUID peerKey) {
		this.peerKey = peerKey;
	}

	private float timePassed = 0;

	@Override
	protected void onUpdate(float tpf) {
		super.onUpdate(tpf);

		Scene scene = findScene();

		final Map<Direction, Boolean> tmp = new EnumMap<Direction, Boolean>(
				Direction.class);
		tmp.put(Direction.North, scene.isPressed(KeyEvent.VK_UP));
		tmp.put(Direction.South, scene.isPressed(KeyEvent.VK_DOWN));
		tmp.put(Direction.West, scene.isPressed(KeyEvent.VK_LEFT));
		tmp.put(Direction.East, scene.isPressed(KeyEvent.VK_RIGHT));

		if (scene.processor().hasSession() && timePassed >= 0.04f
				&& !tmp.equals(inputMap)) {
			Session<SceneProcessor> s = scene.processor().session();

			inputMap.putAll(tmp);

			GridChange o = new GridChange(tmp, peerKey);

			s.applyChange(o);

			timePassed = 0;
		} else {
			timePassed += tpf;
		}
	}
}
