package com.medisons.dbm;

import com.coxautodev.graphql.tools.SchemaParser;
import graphql.schema.GraphQLSchema;
import graphql.servlet.GraphQLInvocationInputFactory;
import graphql.servlet.GraphQLObjectMapper;
import graphql.servlet.GraphQLQueryInvoker;
import graphql.servlet.SimpleGraphQLHttpServlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(name = "GraphQLServlet", urlPatterns = {"graphql"}, loadOnStartup = 1)
public class GraphQLServlet extends SimpleGraphQLHttpServlet {

    private static final SignalDataRepository signalDataRepository;

    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/signals";

    static {
        signalDataRepository = new SignalDataRepository(ConnectionManager.getConnection(CONNECTION_URL));
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
}