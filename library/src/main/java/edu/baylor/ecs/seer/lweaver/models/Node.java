package edu.baylor.ecs.seer.lweaver.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The node class defines the graph structure for modelling java program flow
 * in seer-cfgg. It is similar to a tree structure; however, it does allow
 * back-edges which will produce cycles -- this is necessary to properly track
 * program flow and allow loop detection.
 *
 * Each node has 5 attributes -- id, type, parents, children, and height. A
 * node's id is its identifier, and the form will vary based on type. For a
 * general type node, the id will likely be a reference to a bytecode
 * instruction number, or some other generally meaningless value. For a
 * decision type node, the id will be the decision condition. For a method
 * type node, the id will be the method name (possibly in truncated form).
 *
 * A node's type can be general, decision, or method. These types are used
 * to track different concerns in the flow graph. General type nodes are
 * simply stopping points along the flow, and will not contain any more
 * interesting information. Decision type nodes will always have more than
 * one child, as they mark a divergence in program flow paths. These nodes
 * will allow us to identify loops and if statements later on. Finally, method
 * type nodes can each be treated as the root of their own tree, as they will
 * each have their own flow graph. Additionally, the root of any given Node
 * tree should be a method node.
 *
 * Each node tracks both its parents and its children for ease of traversal.
 * Height is used to determine back-edges, and is not updated once set.
 */
public class Node {

    // Data Fields
    private Integer id;
    private String type;
    private String raw;

    // Connector Fields
    private List<Integer> parents;
    private List<Integer> children;

    private Node() {
        this.id = null;
        this.type = null;
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.raw = "";
    }

    public Node(String id, String type) {
        this.id = Integer.valueOf(id);
        this.type = type;
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.raw = "";
    }

    /**
     * Adds a child to the node's list of children. Also updates the parents
     * list on the added child. Returns the modified node for chaining.
     * Updates height of child if updateHeight is true.
     *
     * @param child
     * @return the modified node
     */
    public Node addChild(Node child) {
        if(this.children == null){
            this.children = new ArrayList<>();
        }
        this.children.add(child.id);
        child.parents.add(this.id);

        return this;
    }

    /**
     * Adds a parent to the node's list of parents. Also updates the children
     * list on the added parent. Returns the modified node for chaining.
     *
     * @param parent the parent to be added
     * @return the modified node
     */
    public Node addParent(Node parent) {
        if(this.parents == null){
            this.children = new ArrayList<>();
        }
        this.parents.add(parent.id);
        parent.children.add(this.id);
        return this;
    }

    /**
     * Removes a child from the node's list of children. Also updates the
     * parents list on the removed child. Returns whether a child was removed.
     *
     * @param child the child to be added
     * @return whether a child was removed
     */
    public Boolean removeChild(Node child) {
        child.parents.remove(this.id);
        return this.children.remove(child.id);
    }

    /**
     * Removes a parent from the node's list of parents. Also updates the
     * children list on the removed parent. Returns whether a parent was
     * removed.
     *
     * @param parent the parent to be removed
     * @return whether a parent was removed
     */
    public Boolean removeParent(Node parent) {
        parent.children.remove(this.id);
        return this.parents.remove(parent.id);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Integer> getParents() {
        return parents;
    }

    public void setParents(List<Integer> parents) {
        this.parents = parents;
    }

    public List<Integer> getChildren() {
        return children;
    }

    public void setChildren(List<Integer> children) {
        this.children = children;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

}
