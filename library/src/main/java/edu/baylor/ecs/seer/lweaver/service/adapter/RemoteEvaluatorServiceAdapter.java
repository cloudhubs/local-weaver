package edu.baylor.ecs.seer.lweaver.service.adapter;

import edu.baylor.ecs.seer.lweaver.service.EvaluatorService;

public class RemoteEvaluatorServiceAdapter {

    EvaluatorService service;

    public RemoteEvaluatorServiceAdapter(EvaluatorService service) {
        this.service = service;
    }

    public String deriveStructure() {
        return service.deriveStructure("");
    }

}
