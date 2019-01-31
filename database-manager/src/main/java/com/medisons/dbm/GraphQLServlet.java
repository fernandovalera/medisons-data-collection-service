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
import java.sql.SQLException;

@WebServlet(name = "GraphQLServlet", urlPatterns = {"graphql"}, loadOnStartup = 1)
public class GraphQLServlet extends SimpleGraphQLHttpServlet {

    private static SignalDataRepository signalDataRepository;
    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/signals";

    static {
        try {
            signalDataRepository = new SignalDataRepository(ConnectionManager.getConnection(CONNECTION_URL));
        } catch (SQLException e) {
            // Without a connection to the DB, quit the program.
            System.exit(1);
        }
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
        resp.addHeader("Access-Control-Allow-Origin", "*");
        super.doPost(req, resp);
    }
}