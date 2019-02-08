package edu.baylor.ecs.seer.lweaver.service.adapter;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.lweaver.service.EvaluatorService;

public class RemoteEvaluatorServiceAdapter {

    EvaluatorService service;

    public RemoteEvaluatorServiceAdapter(EvaluatorService service) {
        this.service = service;
    }

    public SeerContext deriveStructure(SeerContext context) {
        String oldPath = context.getRequest().getPathToCompiledMicroservices();
        context.getRequest().setPathToCompiledMicroservices("");

        context = service.deriveStructure(context);

        context.getRequest().setPathToCompiledMicroservices(oldPath);

        return context;
    }

}
