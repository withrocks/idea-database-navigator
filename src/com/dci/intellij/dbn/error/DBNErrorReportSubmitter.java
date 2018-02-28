package com.dci.intellij.dbn.error;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.diagnostic.LogMessage;
import com.intellij.diagnostic.LogMessageEx;
import com.intellij.errorreport.bean.ErrorBean;
import com.intellij.ide.DataManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.FAILED;
import static com.intellij.openapi.diagnostic.SubmittedReportInfo.SubmissionStatus.NEW_ISSUE;

public class DBNErrorReportSubmitter extends ErrorReportSubmitter {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private static final String URL = "http://dci.myjetbrains.com/youtrack/";
    private static final String ISSUE_URL = URL + "rest/issue";
    private static final String LOGIN_URL = URL + "rest/user/login";
    private static final String ENCODING = "UTF-8";
    public static final String LINE_DELIMITER = "\n__________________________________________________________________\n";

    public DBNErrorReportSubmitter() {
        System.out.println();
    }

    @Override
    public IdeaPluginDescriptor getPluginDescriptor() {
        IdeaPluginDescriptor pluginDescriptor = (IdeaPluginDescriptor) super.getPluginDescriptor();
        if (pluginDescriptor == null) {
            pluginDescriptor = PluginManager.getPlugin(PluginId.getId(DatabaseNavigator.DBN_PLUGIN_ID));
            setPluginDescriptor(pluginDescriptor);
        }
        return pluginDescriptor;
    }

    @Override
    public String getReportActionText() {
        return "Submit Issue Report";
    }

    public SubmittedReportInfo submit(IdeaLoggingEvent[] events, Component parentComponent) {
        final SubmittedReportInfo[] reportInfo = new SubmittedReportInfo[1];
        Consumer<SubmittedReportInfo> consumer = new Consumer<SubmittedReportInfo>() {
            @Override
            public void consume(SubmittedReportInfo submittedReportInfo) {
                reportInfo[0] = submittedReportInfo;
            }
        };
        String additionalInfo = ((LogMessage)events[0].getData()).getAdditionalInfo();
        submit(events, additionalInfo, parentComponent, consumer);
        return reportInfo[0];
    }

    public boolean submit(@NotNull IdeaLoggingEvent[] events, String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<SubmittedReportInfo> consumer) {
        DataContext dataContext = DataManager.getInstance().getDataContext(parentComponent);
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);

        String localPluginVersion = getPluginDescriptor().getVersion();
        String repositoryPluginVersion = DatabaseNavigator.getInstance().getRepositoryPluginVersion();

        if (repositoryPluginVersion != null && repositoryPluginVersion.compareTo(localPluginVersion) > 0) {
            NotificationUtil.sendWarningNotification(project, Constants.DBN_TITLE_PREFIX + "New Plugin Version Available", "A newer version of Database Navigator plugin is available in repository" + ". Error report not sent.");
            consumer.consume(new SubmittedReportInfo(ISSUE_URL, "", FAILED));
            return false;
        }

        IdeaLoggingEvent event = events[0];
        String eventText = event.getThrowableText();
        String summary = eventText.substring(0, Math.min(Math.max(80, eventText.length()), 80));

        String platformBuild = ApplicationInfo.getInstance().getBuild().asString();

        @NonNls StringBuilder description = new StringBuilder();
        description.append("Java Version: ").append(System.getProperty("java.version")).append('\n');
        description.append("Operating System: ").append(System.getProperty("os.name")).append('\n');
        description.append("IDE Version: ").append(platformBuild).append('\n');
        description.append("DBN Version: ").append(localPluginVersion).append("\n");
        description.append("Last Action Id: ").append(IdeaLogger.ourLastActionId).append("\n");

        if (StringUtil.isNotEmpty(additionalInfo)) {
            description.append("\n\nUser Message:");
            description.append(LINE_DELIMITER);
            description.append(additionalInfo);
            description.append(LINE_DELIMITER);
        }

        description.append("\n\n").append(event.toString());

        Object eventData = event.getData();
        if (eventData instanceof LogMessageEx) {
            List<Attachment> attachments = ((LogMessageEx) eventData).getAttachments();
            if (attachments.size() > 0) {
                Set<String> attachmentTexts = new HashSet<String>();
                for (Attachment attachment : attachments) {
                    attachmentTexts.add(attachment.getDisplayText().trim());
                }

                description.append("\n\nAttachments:");
                description.append(LINE_DELIMITER);
                int index = 0;
                for (String attachmentText : attachmentTexts) {
                    if (index > 0) description.append(LINE_DELIMITER);
                    description.append("\n");
                    description.append(attachmentText);
                    index++;
                }

                description.append(LINE_DELIMITER);
            }
        }


        String result = null;
        try {
            result = submit(events, localPluginVersion, summary, description.toString());
        } catch (Exception e) {

            NotificationUtil.sendErrorNotification(project, Constants.DBN_TITLE_PREFIX + "Error Reporting",
                    "<html>Failed to send error report: "+ e.getMessage() + "</html>");

            consumer.consume(new SubmittedReportInfo(ISSUE_URL, "", FAILED));
            return false;
        }

        LOGGER.info("Error report submitted, response: " + result);

        String ticketId = null;
        try {
            Pattern regex = Pattern.compile("id=\"([^\"]+)\"", Pattern.DOTALL | Pattern.MULTILINE);
            Matcher regexMatcher = regex.matcher(result);
            if (regexMatcher.find()) {
                ticketId = regexMatcher.group(1);
            }
        } catch (PatternSyntaxException e) {
            NotificationUtil.sendErrorNotification(project, Constants.DBN_TITLE_PREFIX + "Error Reporting", "Failed to receive error report confirmation");
            consumer.consume(new SubmittedReportInfo(ISSUE_URL, "", FAILED));
            return false;
        }

        String ticketUrl = URL + "issue/" + ticketId;
        NotificationUtil.sendInfoNotification(project, Constants.DBN_TITLE_PREFIX + "Error Reporting",
                "<html>Error report successfully sent. Ticket <a href='" + ticketUrl + "'>" + ticketId + "</a> created.</html>");

        consumer.consume(new SubmittedReportInfo(ticketUrl, ticketId, NEW_ISSUE));
        return true;
    }

    @NotNull
    public String submit(@NotNull IdeaLoggingEvent[] events, String pluginVersion, String summary, String description) throws Exception{
        StringBuilder response = new StringBuilder("");

        ErrorBean errorBean = new ErrorBean(events[0].getThrowable(), IdeaLogger.ourLastActionId);
        Object eventData = events[0].getData();
        if (eventData instanceof LogMessageEx) {
            errorBean.setAttachments(((LogMessageEx)eventData).getAttachments());
        }

        Map<String, String> parameters = createParameters(summary, description, pluginVersion, errorBean);
        byte[] output = join(parameters);
        URL issueUrl = new URL(ISSUE_URL);
        URLConnection issueConnection = issueUrl.openConnection();
        issueConnection.setDoOutput(true);

        OutputStream outputStream = issueConnection.getOutputStream();
        try {
            outputStream.write(output);
        } finally {
            outputStream.close();
        }

        BufferedReader responseReader = new BufferedReader(new InputStreamReader(issueConnection.getInputStream()));

        String line;
        while ((line = responseReader.readLine()) != null) {
            response.append(line);
        }
        return response.toString();
    }

    private static Map<String, String> createParameters(String summary, String description, String pluginVersion, ErrorBean error) {
        Map<String, String> params = ContainerUtil.newLinkedHashMap();

        params.put("login", "autosubmit");
        params.put("password", "autosubmit");

        params.put("project", "DBN");
        params.put("assignee", "Unassigned");
        params.put("summary", summary);
        params.put("description", description);
        params.put("priority", "4");
        params.put("type", "Exception");

        if (pluginVersion != null)                     {
            params.put("affectsVersion", pluginVersion);
        }
        return params;
    }

    private static String format(Calendar calendar) {
        return calendar == null ?  null : Long.toString(calendar.getTime().getTime());
    }

    private static byte[] join(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (com.intellij.openapi.util.text.StringUtil.isEmpty(param.getKey())) {
                throw new IllegalArgumentException(param.toString());
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            if (com.intellij.openapi.util.text.StringUtil.isNotEmpty(param.getValue())) {
                builder.append(param.getKey()).append('=').append(URLEncoder.encode(param.getValue(), ENCODING));
            }
        }
        return builder.toString().getBytes(ENCODING);
    }
}