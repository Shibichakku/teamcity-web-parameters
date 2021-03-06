package ru.mail.teamcity.web.parameters.controller;

import jetbrains.buildServer.controllers.parameters.InvalidParametersException;
import jetbrains.buildServer.controllers.parameters.ParameterEditContext;
import jetbrains.buildServer.controllers.parameters.ParameterRenderContext;
import jetbrains.buildServer.controllers.parameters.api.ParameterControlProviderAdapter;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
import ru.mail.teamcity.web.parameters.data.Options;
import ru.mail.teamcity.web.parameters.manager.WebOptionsManager;
import ru.mail.teamcity.web.parameters.parser.ParserFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * User: g.chernyshev
 * Date: 27.06.14
 * Time: 17:51
 */
public class WebParameterController extends ParameterControlProviderAdapter {

    @NotNull
    private final PluginDescriptor pluginDescriptor;
    @NotNull
    private final WebOptionsManager webOptionsManager;
    @NotNull
    private final Map<String, String> errors;


    public WebParameterController(
            @NotNull PluginDescriptor pluginDescriptor,
            @NotNull WebOptionsManager webOptionsManager
    ) {
        this.pluginDescriptor = pluginDescriptor;
        this.webOptionsManager = webOptionsManager;
        this.errors = new HashMap<String, String>();
    }

    @NotNull
    @Override
    public String getParameterType() {
        return "webPopulatedSelect";
    }

    @NotNull
    @Override
    public String getParameterDescription() {
        return "Web populated select";
    }

    @NotNull
    @Override
    public ModelAndView renderControl(@NotNull HttpServletRequest request, @NotNull ParameterRenderContext context) throws InvalidParametersException {
        ModelAndView modelAndView = new ModelAndView(pluginDescriptor.getPluginResourcesPath("ru/mail/teamcity/web/parameters/jsp/webParameterControl.jsp"));

        String url = context.getDescription().getParameterTypeArguments().get("url");
        String format = context.getDescription().getParameterTypeArguments().get("format");
        Boolean enableEditOnError;
        if (context.getDescription().getParameterTypeArguments().containsKey("enableEditOnError")) {
            enableEditOnError = context.getDescription().getParameterTypeArguments().get("enableEditOnError").equalsIgnoreCase("true") ? true : false;
        } else {
            enableEditOnError = false;
        }

        errors.clear();
        Options options = webOptionsManager.read(url, format, errors);
        modelAndView.getModel().put("options", options);
        modelAndView.getModel().put("enableEditOnError", enableEditOnError);
        modelAndView.getModel().put("errors", errors);
        return modelAndView;
    }

    @NotNull
    @Override
    public ModelAndView renderSpecEditor(@NotNull HttpServletRequest request, @NotNull ParameterEditContext parameterEditContext) throws InvalidParametersException {
        ModelAndView modelAndView = new ModelAndView(pluginDescriptor.getPluginResourcesPath("ru/mail/teamcity/web/parameters/jsp/webParameterConfiguration.jsp"));
        modelAndView.getModel().put("parsers", ParserFactory.registry);
        return modelAndView;
    }
}