package com.miany.psycheecho;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.miany.psycheecho.content.EchoCategory;
import com.miany.psycheecho.content.EchoNote;
import run.halo.app.extension.Scheme;

import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author miany
 * @since 1.0.0
 */
@Component
@ComponentScan(basePackages = "com.miany.psycheecho")
public class PsycheEchoPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public PsycheEchoPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(EchoNote.class);
        schemeManager.register(EchoCategory.class);
        System.out.println("插件启动成功！");
    }

    @Override
    public void stop() {
        Scheme categoryScheme = schemeManager.get(EchoCategory.class);
        if (categoryScheme != null) {
            schemeManager.unregister(categoryScheme);
        }
        Scheme noteScheme = schemeManager.get(EchoNote.class);
        if (noteScheme != null) {
            schemeManager.unregister(noteScheme);
        }
        System.out.println("插件停止！");
    }
}
