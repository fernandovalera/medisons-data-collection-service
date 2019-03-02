package com.medisons.dbm;

import com.coxautodev.graphql.tools.SchemaParser;
import graphql.schema.GraphQLSchema;
import graphql.servlet.GraphQLInvocationInputFactory;
import graphql.servlet.GraphQLObjectMapper;
import graphql.servlet.GraphQLQueryInvoker;
import graphql.servlet.SimpleGraphQLHttpServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GraphQLServlet", urlPatterns = {"graphql"}, loadOnStartup = 1)
public class GraphQLServlet extends SimpleGraphQLHttpServlet {

    private static SignalDataRepository signalDataRepository;
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/signals?serverTimezone=UTC";

    // Response header keys and values
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_KEY = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_VALUE = "*";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER_KEY = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER_VALUE =
            "apollographql-client-name,apollographql-client-version,content-type";

    static {
        ConnectionManager connectionManager = new HikariConnectionManager(CONNECTION_URL);

        signalDataRepository = new SignalDataRepository(connectionManager);
    }

    public GraphQLServlet() {
        super(invocationInputFactory(), queryInvoker(), objectMapper(), null, false);
    }

    private static GraphQLInvocationInputFactory invocationInputFactory() {
        return GraphQLInvocationInputFactory.newBuilder(createSchema()).build();
    }

    private static GraphQLSchema createSchema() {
        return SchemaParser.newParser()
                .file("schema.graphqls")
                .resolvers(new Query(signalDataRepository), new Mutation(signalDataRepository))
                .build()
                .makeExecutableSchema();
    }

    private static GraphQLQueryInvoker queryInvoker() {
        return GraphQLQueryInvoker.newBuilder().build();
    }

    private static GraphQLObjectMapper objectMapper() {
        return GraphQLObjectMapper.newBuilder().build();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_KEY, ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_VALUE);
        resp.addHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER_KEY, ACCESS_CONTROL_ALLOW_HEADERS_HEADER_VALUE);
        super.doPost(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_KEY, ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_VALUE);
        resp.addHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER_KEY, ACCESS_CONTROL_ALLOW_HEADERS_HEADER_VALUE);
        super.doOptions(req, resp);
    }
}