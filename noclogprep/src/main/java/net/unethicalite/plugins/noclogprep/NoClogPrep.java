package net.unethicalite.plugins.noclogprep;

import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.plugins.Task;
import org.pf4j.Extension;

// This annotation is required in order for the client to detect it as a plugin/script.
@PluginDescriptor(name = "No Clog Prep", enabledByDefault = false)
@Extension
public class NoClogPrep extends Script
{
	private static final Task[] TASKS = new Task[]{

	};

	@Override
	public void onStart(String... args)
	{
	}

	@Override
	protected int loop()
	{
		for (Task task : TASKS)
		{
			if (task.validate())
			{
				int sleep = task.execute();

				if (sleep == -1)  // jank; using this as an error code
				{
					this.logger.warn("Task failed: "+task.getClass().getName()+". Script paused.");
					this.setPaused(true);
				}

				if (task.isBlocking())
				{
					return sleep;
				}
			}
		}

		return 1000;
	}
}
