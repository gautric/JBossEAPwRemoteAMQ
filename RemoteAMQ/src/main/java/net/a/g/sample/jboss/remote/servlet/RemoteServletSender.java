package net.a.g.sample.jboss.remote.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/send")
public class RemoteServletSender extends HttpServlet {

    private static final long serialVersionUID = -8314035702649252239L;

    private static final int MSG_COUNT = 5;

    @Inject
    @JMSConnectionFactory("java:comp/env/RemoteConnectionFactory")
    private JMSContext context;

    @Resource(lookup = "java:comp/env/RemoteQueue")
    private Queue remoteQueue;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        try {
            final Destination destination = remoteQueue;

            for (int i = 1; i <= MSG_COUNT; i++) {
                String text = "This is message " + i;
                context.createProducer().send(destination, text);
            }
            out.write("Sending "+MSG_COUNT+" messages to " + destination + "");

        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
