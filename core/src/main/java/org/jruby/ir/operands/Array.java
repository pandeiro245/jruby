package org.jruby.ir.operands;

import org.jruby.ir.IRVisitor;
import org.jruby.ir.transformations.inlining.InlinerInfo;
import org.jruby.parser.StaticScope;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

import java.util.List;
import java.util.Map;

// Represents an array [_, _, .., _] in ruby
//
// NOTE: This operand is only used in the initial stages of optimization.
// Further down the line, this array operand could get converted to calls
// that actually build a Ruby object
public class Array extends Operand {
    private final Operand[] elts;

    // SSS FIXME: Do we create a special-case for zero-length arrays?
    public Array() {
        this(new Operand[0]);
    }

    public Array(List<Operand> elts) {
        this(elts.toArray(new Operand[elts.size()]));
    }

    public Array(Operand[] elts) {
        super(OperandType.ARRAY);

        this.elts = elts == null ? new Operand[0] : elts;
    }

    public boolean isBlank() {
        return elts.length == 0;
    }

    public int size() {
        return elts.length;
    }

    public Operand get(int i) {
        return elts[i];
    }

    @Override
    public String toString() {
        return "Array:" + (isBlank() ? "[]" : java.util.Arrays.toString(elts));
    }

// ---------- These methods below are used during compile-time optimizations -------
    @Override
    public boolean hasKnownValue() {
        for (Operand o : elts) {
            if (!o.hasKnownValue()) return false;
        }

        return true;
    }

    @Override
    public Operand getSimplifiedOperand(Map<Operand, Operand> valueMap, boolean force) {
        Operand[] newElts = new Operand[elts.length];
        for (int i = 0; i < elts.length; i++) {
            newElts[i] = elts[i].getSimplifiedOperand(valueMap, force);
        }

        return new Array(newElts);
    }

    public Operand toArray() {
        return this;
    }

    /** Append the list of variables used in this operand to the input list */
    @Override
    public void addUsedVariables(List<Variable> l) {
        for (Operand o: elts) {
            o.addUsedVariables(l);
        }
    }

    @Override
    public Operand cloneForInlining(InlinerInfo ii) {
        if (hasKnownValue()) return this;

        Operand[] newElts = new Operand[elts.length];
        for (int i = 0; i < elts.length; i++) {
            newElts[i] = elts[i].cloneForInlining(ii);
        }

        return new Array(newElts);
    }

    @Override
    public Object retrieve(ThreadContext context, IRubyObject self, StaticScope currScope, DynamicScope currDynScope, Object[] temp) {
        IRubyObject[] elements = new IRubyObject[elts.length];

        for (int i = 0; i < elements.length; i++) {
            elements[i] = (IRubyObject) elts[i].retrieve(context, self, currScope, currDynScope, temp);
        }

        return context.runtime.newArray(elements);
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.Array(this);
    }

    public Operand[] getElts() {
        return elts;
    }
}
