package chire.python;

import chire.python.stmt.PyStatement;
import chire.python.stmt.block.*;
import chire.python.stmt.content.control.*;
import chire.python.stmt.content.*;
import chire.python.stmt.content.decl.*;
import chire.python.stmt.content.expr.*;
import chire.python.stmt.type.*;
import chire.python.util.type.TypeChecker;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class PyParser {
    private final CommonTokenStream tokenStream;

    public static ArrayList<PyStatement> statements = new ArrayList<>();

    private int current = 0;

    public PyParser(CommonTokenStream token) {
        this.tokenStream = token;
    }

    public boolean isEnd(){
        return peek().getType() == -1;
    }

    public ArrayList<PyStatement> parse(){
        while (!isEnd()){
            statements.addAll(bodyDeclaration());

            current++;
        }

        return statements;
    }

    private PyStatement forDeclaration() {
        return forDeclaration(0);
    }

    private PyStatement forDeclaration(int cur) {
        this.current += cur;

        //TODO 这里只支持了单变量循环，之后修改
        if (peek().getType() != 45) throw new RuntimeException("no key");
        Token variable = peek();

        current += 2;

        PyStatement iterable;

        if (match(current+1, 44, 63, 88, 89, 90, 92, 60)){
            iterable = varDeclaration();
        } else if (match(current+1, 57)){
            iterable = (methodCall());
        } else if (match(current+1, 54)) {
            iterable = (submethodCall(varCall()));
        } else {
            throw new RuntimeException("no key");
        }

        ArrayList<PyStatement> body = new ArrayList<>();

        while (!isEnd()) {
            switch (peek().getType()) {
                case 2:
                    return new ForStatement(variable, iterable, body);

                default:
                    body.addAll(bodyDeclaration());
                    break;
            }

            current++;
        }

        throw new RuntimeException("no key");
//        return new ForStatement(variable, iterable, bodyDeclaration(3));
    }

    private PyStatement importDeclaration() {
        return importDeclaration(0);
    }

    private PyStatement importDeclaration(int cur) {
        this.current += cur;

        var key = peek();
        ImportStatement importStatement = null;
        switch (key.getType()){
            case 23:
                StringBuilder path = new StringBuilder();

                for (;;) {
                    current++;
                    key = peek();

                    if (key.getType() == 54) {
                        //TODO 这里之后要支持父级调用
                        if (last().getType() == 54 || previous().getType() == 54) throw new RuntimeException("not key");
                        path.append(".");
                        continue;
                    }

                    if (key.getType() == 45) {
                        path.append(key.getText());
                        continue;
                    }

                    if (key.getType() == 26) {
                        break;
                    } else {
                        throw new RuntimeException("out key");
                    }
                }

                current++;

                key = peek();

                importStatement = new ImportStatement(path.toString(), key.getText());
                break;

            case 26:
//                List<String> pack = new ArrayList<>();
                StringBuilder pack = new StringBuilder();

                for (;;) {
                    current++;
                    key = peek();

                    if (key.getType() == 54) {
                        //TODO 这里之后要支持父级调用
                        if (last().getType() == 54 || previous().getType() == 54) throw new RuntimeException("not key");
                        pack.append(".");
                        continue;
                    }

                    if (key.getType() == 45) {
                        if (key.getType() != 54 && last().getType() != 54) {
                            importStatement = new ImportStatement(pack.toString(), key.getText());
                            break;
                        }

                        pack.append(key.getText());
                        continue;
                    }

                    throw new RuntimeException("out key");
                }

                break;
        }

        if (last().getType() == 7) {
            current += 2;

            if (key.getType() == 45) {
                importStatement.toName(key.getText());
                return importStatement;
            } else {
                throw new RuntimeException("out key");
            }
        }

        return importStatement;
    }

    private PyStatement submethodCall(PyStatement var){
        current += 2;
        PyStatement call;
        if (match(current+1, 57)) {
            call = methodCall();
        } else {
            call = varCall();
        }

        if (match(current+1, 63)) {
            current++;
            return new SubSetStatement(var, call, assignment(1));
        }

        if (last().getType() == 54) {
            return submethodCall(
                    new SubCallStatement(var, call)
            );
        }

        return new SubCallStatement(var, call);
    }

    private ArrayList<PyStatement> bodyDeclaration(){
        return bodyDeclaration(0);
    }

    private final int[] bodyIndex = new int[]{
            45, 15, 25, 41, 13, 23, 26, 22
    };
    private ArrayList<PyStatement> bodyDeclaration(int cur){
        this.current += cur;

        ArrayList<PyStatement> body = new ArrayList<>();

        var key = peek();
        switch (key.getType()) {
            case 45:
                if (match(current+1, 63, 88, 89, 90, 92, 60, 64)){
                    body.add(varDeclaration());
                } else if (match(current+1, 57)){
                    body.add(methodCall());
                } else if (match(current+1, 54)) {
                    body.add(submethodCall(varCall()));
                }
                break;

            case 15:
                body.add(defDeclaration());
                break;

            case 25:
                body.add(ifDeclaration());
                break;

            case 41:
                body.add(whileDeclaration());
                break;

            case 13:
                body.add(classDeclaration(1));
                break;

            case 23, 26:
                body.add(importDeclaration());
                break;

            case 22:
                body.add(forDeclaration(1));
                break;

            default:
                break;
        }

        return body;
    }

    private PyStatement classDeclaration(){
        return classDeclaration(0);
    }

    private PyStatement classDeclaration(int cur){
        this.current += cur;

        Token class_token = peek();

        if (class_token.getType() != 45) throw new RuntimeException("no 45 key");

        ArrayList<PyStatement> body = new ArrayList<>();

        while (!isEnd()) {
            current++;

            if (match(current, bodyIndex)) {
                body.addAll(bodyDeclaration());
                continue;
            }

            switch (peek().getType()) {
                case 1://"    "
                case 44:
                case 60:
                    break;

                case 2:
                    return new ClassStatement(class_token, body);

                default:
                    break;
            }
        }

        return null;
    }

    private PyStatement whileDeclaration(){
        return whileDeclaration(0);
    }

    private PyStatement whileDeclaration(int cur){
        this.current += cur;

        var ifStmt = ifCondition();
        ArrayList<PyStatement> body = new ArrayList<>();

        while (!isEnd()) {
            if (match(current, bodyIndex)) {
                body.addAll(bodyDeclaration());
            } else {
                switch (peek().getType()) {
                    case 44, 1:
                        break;

                    case 11:
                        body.add(new BreakStatement());
                        break;

                    case 37:
                        if (match(this.current + 1, 44)) {
                            body.add(new ReturnStatement());
                        } else {
                            body.add(new ReturnStatement(assignment(1)));
                        }
                        break;

                    case 2:
                        return new WhileStatement(ifStmt, body);

                    default:
                        break;
                }
            }

            this.current++;
        }

        return null;
    }

    private ArrayList<ArgStatement> argsDeclaration(){
        ArrayList<ArgStatement> args = new ArrayList<>();

        while (!isEnd()) {
            current++;

            var token = peek();

            switch (token.getType()) {
                case 45:
                    FunStatement.TypeStatement type;
                    if (match(this.current+1, 60)){
                        current+=2;
                        if (!match(current, 45, 3)) throw new RuntimeException("no key");
                        type = typeDeclaration();
                    } else {
                        type = null;
                    }

                    args.add(new ArgStatement(token, type));
                    break;

                case 59:
                    break;
                case 58:
                    return args;
            }
        }

        throw new RuntimeException("no args");
    }

    private FunStatement defDeclaration(){
        current++;

        var token = peek();

        if (token.getType() == 45) {
            ArrayList<ArgStatement> args = argsDeclaration();
            ArrayList<PyStatement> body = new ArrayList<>();

            while (!isEnd()) {
                current++;
                var peek = peek();

                if (match(current, bodyIndex)) {
                    body.addAll(bodyDeclaration());
                    continue;
                }

                switch (peek.getType()) {
                    case 1://"    " 长度不固定，根据缩进确定
                    case 44:
                    case 60:
                        break;

                    case 2:
                        return new FunStatement(token, args, body);

                    case 37:
                        if (match(this.current+1, 44)) {
                            body.add(new ReturnStatement());
                        } else {
                            body.add(new ReturnStatement(assignment(1)));
                        }
                        break;

                    case 23, 26:
                        body.add(importDeclaration());
                        break;
                }
            }
        }
        throw new RuntimeException("no key");
    }

    private FunStatement.TypeStatement typeDeclaration(){
        return typeDeclaration(0);
    }
    private FunStatement.TypeStatement typeDeclaration(int cur){
        this.current += cur;

        Token token = peek();

        switch (token.getType()) {
            case 45, 3:
                return new FunStatement.TypeStatement(token);
        }

        throw new NullPointerException("no key");
    }

    private PyStatement varDeclaration(){
        current++;

        var key = peek();

        switch (key.getType()){
            case 63:
                var name = previous();
                var asm = assignment(1);
                return new VarStatement(name, asm);

            case 60:
                name = previous();
                var type = typeDeclaration(1);
                current++;
                asm = assignment(1);
                return new VarStatement(name, asm, type);

            case 64:
                name = previous();
                var index = assignment(1);
                current += 2;
                if (match(current, 63)) {
                    asm = assignment(1);
                    return new VarStatement(name, index, asm, null);
                } else {
                    throw new RuntimeException("no key");
                }


            //TODO '@='=91 ? 这是什么鬼
            case 88, 89, 90, 92:
                String operator;

                if (key.getType() == 88) {
                    operator = "+";
                } else if (key.getType() == 89) {
                    operator = "-";
                } else if (key.getType() == 90) {
                    operator = "*";
                } else if (key.getType() == 92) {
                    operator = "/";
                } else {
                    throw new RuntimeException("Characters are not recognized");
                }

                return new VarStatement(previous(),
                        new LogicalStatement(
                                new VarCallStatement(previous()),
                                operator,
                                assignment(1)
                        ));

            case 44:
                //TODO 这里在创建时未处理空结构的问题。很明显，我应该默认None
                throw new RuntimeException("parser error in "+key);
//                return new VarStatement(previous(), null);

            default:
                throw new RuntimeException("parser error in "+key);
        }
    }

    private PyStatement ifDeclaration(){
        return ifDeclaration(0);
    }

    private IfStatement ifDeclaration(int cur){
        this.current += cur;

        var ifStmt = ifCondition();
        ArrayList<PyStatement> body = new ArrayList<>();
        IfStatement elseStmt = null;

        while (!isEnd()) {
            if (match(current, bodyIndex)) {
                body.addAll(bodyDeclaration());
            } else {
                switch (peek().getType()) {
                    case 44, 1:
                        break;

                    case 37:
                        if (match(this.current + 1, 44)) {
                            body.add(new ReturnStatement());
                        } else {
                            body.add(new ReturnStatement(assignment(1)));
                        }
                        break;

                    case 11:
                        body.add(new BreakStatement());
                        break;

                    case 2:
                        if (last().getType() == 17) {
                            elseStmt = ifDeclaration(1);
                        } else if (last().getType() == 18) {
                            elseStmt = ifDeclaration(1);
                        }

                        return new IfStatement(ifStmt, body, elseStmt);

                    case 23, 26:
                        body.add(importDeclaration());
                        break;

                    default:
                        break;
                }
            }

            this.current++;
        }

        throw new RuntimeException("no key");
    }

    private PyStatement ifCondition(){
        PyStatement left = null;
        Token operator = null;
        PyStatement right = null;

        while (!isEnd()) {
            switch (peek().getType()) {
                case 45, 4, 3:
                    if (left != null) {
                        right = assignment();
                    } else {
                        left = assignment();
                    }
                    break;

                case 79, 80, 81, 82, 83, 85:
                    operator = peek();
                    break;

                case 60:
                    if (operator != null && right != null) {
                        return new IfStatement.JudgmentStatement(left, operator, right);
                    } else if (operator == null && right == null){
                        return left;
                    } else {
                        throw new RuntimeException("no key?");
                    }

                case 38, 20:
                    var key = peek();
                    if (Objects.equals(key.getText(), "True")) {
                        return new ConstStatement<>(key, Boolean.class);
                    } else if (Objects.equals(key.getText(), "False")){
                        return new ConstStatement<>(key, Boolean.class);
                    } else {
                        throw new RuntimeException("parser error");
                    }
            }

            this.current++;
        }

        throw new RuntimeException("no key?");
    }

    private PyStatement assignment(){
        return assignment(0);
    }

    private PyStatement assignment(int cur){
        this.current += cur;

        var key = peek();

        switch (key.getType()) {
            case 3:
                return new ConstStatement<>(key, String.class);
            case 4, 72, 71: // TODO 与下方的 45, 64 存在功能差分的情况，需要进一步提取
                boolean range = true;

                if (match(current, 71, 72)) {
                    range = peek().getType() != 72;
                    current++;
                    key = peek();
                }

                PyStatement constStmt;

                if (TypeChecker.isInteger(key.getText())) {
                    constStmt = new NumberStatement<>(range, key, Integer.class);
                } else if (TypeChecker.isFloatingPointNumber(key.getText())) {
                    constStmt = new NumberStatement<>(range, key, Float.class);
                } else {
                    throw new RuntimeException("parser error "+peek());
                }

                if (match(this.current+1, 71, 72, 73, 56, 62)) {
                    current++;

                    return logicalAssignment(constStmt);
                } else {
                    return constStmt;
                }
            case 38, 20:
                if (Objects.equals(key.getText(), "True")) {
                    return new ConstStatement<>(key, Boolean.class);
                } else if (Objects.equals(key.getText(), "False")){
                    return new ConstStatement<>(key, Boolean.class);
                } else {
                    throw new RuntimeException("parser error");
                }

            case 45, 64:
                PyStatement varmetStmt;

                if (key.getType() == 64) {
                    varmetStmt = listAssignment();
                } else if (match(this.current+1, 57)){
                    varmetStmt = methodCall();
                } else if (match(this.current+1, 64)) {
                    current++;
                    varmetStmt = new IndexStatement(key, assignment(1));

                    if (last().getType() == 65) current++;
                    else throw new RuntimeException("no key");
                }
                else {
                    varmetStmt = varCall();
                }

                if (match(this.current+1, 71, 72, 73, 56, 62)) {
                    current++;

                    return logicalAssignment(varmetStmt);
                } else if (match(current+1, 54)) {
                    return submethodCall(varCall());
                } else {
                    return varmetStmt;
                }

            case 31:
                return new NoneStatement();

            case 57:
                return tupleDeclaration();

            case 77:
                return dictDeclaration();

            default:
                throw new RuntimeException("parser error in "+key);
        }
    }

    private PyStatement logicalAssignment(PyStatement statement) {
        while (!isEnd()) {
            Token operator = peek();

            PyStatement right = blockLogicalStatement(1);

            if (match(last().getTokenIndex(), 73, 56, 62)) {
                statement = new LogicalStatement(
                        statement,
                        operator,
                        new LogicalStatement(right, peek(), blockLogicalStatement(1))
                );
            } else {
                statement = new LogicalStatement(statement, operator, right);
            }

            if (match(last().getTokenIndex(), 44,  1, 65, 58, 60)) {
                return statement;
            } else {
                current++;
            }
        }

        throw new RuntimeException("parser error");
    }

    private PyStatement blockLogicalStatement(int current) {
        this.current += current;

        var key = peek();

        switch (key.getType()) {
            case 3:
                return new ConstStatement<>(key, String.class);
            case 4:
                if (TypeChecker.isInteger(key.getText())) {
                    return new NumberStatement<>(key, Integer.class);
                } else if (TypeChecker.isFloatingPointNumber(key.getText())) {
                    return new NumberStatement<>(key, Float.class);
                } else {
                    throw new RuntimeException("parser error");
                }

            case 57:
                return assignment(1);

            //TODO 4, 72, 71 对于块的解析仍然存在问题，但大部分情况下可以正常运行。
            case 45, 64:
                PyStatement varmetStmt;

                if (key.getType() == 64) {
                    varmetStmt = listAssignment();
                } else if (match(this.current+1, 57)){
                    varmetStmt = methodCall();
                } else if (match(this.current+1, 64)) {
                    this.current++;
                    varmetStmt = new IndexStatement(key, assignment(1));

                    if (last().getType() == 65) this.current++;
                    else throw new RuntimeException("no key");
                }
                else {
                    varmetStmt = varCall();
                }

                return varmetStmt;

            default:
                throw new RuntimeException("parser error");
        }
    }

    private PyStatement varCall(){
        return varCall(0);
    }

    private PyStatement varCall(int current){
        this.current += current;
        return new VarCallStatement(peek());
    }

    private PyStatement dictDeclaration() {
        Map<PyStatement, PyStatement> args = new LinkedHashMap<>();

        while (!isEnd()) {
            current++;
            switch (peek().getType()) {
                case 4, 3, 38, 45:
                    args.put(assignment(), assignment(2));
                    break;

                case 77:
                    break;
                case 78:
                    return new DictStatement(args);
            }
        }

        throw new RuntimeException("no args");
    }

    private PyStatement tupleDeclaration() {
        ArrayList<PyStatement> args = new ArrayList<>();

        while (!isEnd()) {
            current++;
            switch (peek().getType()) {
                case 4, 3, 38, 45:
                    args.add(assignment());
                    break;

                case 59:
                    break;
                case 58:
                    return new TupleStatement(args);
            }
        }

        throw new RuntimeException("no args");
    }

    private PyStatement listAssignment() {
        return new ListStatement(keyDeclaration());
    }

    private ArrayList<PyStatement> keyDeclaration(){
        ArrayList<PyStatement> args = new ArrayList<>();

        while (!isEnd()) {
            current++;
            switch (peek().getType()) {
                case 4, 3, 38, 45:
                    args.add(assignment());
                    break;

                case 64:
                    break;
                case 65:
                    return args;
            }
        }

        throw new RuntimeException("no args");
    }

    private ArrayList<PyStatement> argsCallDeclaration(){
        ArrayList<PyStatement> args = new ArrayList<>();

        while (!isEnd()) {
            current++;
            switch (peek().getType()) {
                case 4, 3, 38:
                    args.add(assignment());
                    break;

                case 45:
                    if (last().getType() == 63) {
                        args.add(new ParametersStatement(peek(), assignment(2)));
                    } else {
                        args.add(assignment());
                    }
                    break;

                case 59:
                    break;
                case 58:
                    return args;
            }
        }

        throw new RuntimeException("no args");
    }

    private FunCallStatement methodCall(){
        var funCall = new FunCallStatement(peek());

        current += 1;

        funCall.setArg(argsCallDeclaration());

        return funCall;
    }

    private Token peek() {
        return tokenStream.get(current);
    }

    private Token previous() {
        return tokenStream.get(current - 1);
    }

    private Token last(){
        return tokenStream.get(current + 1);
    }

    private Token advance() {
        if (!isEnd()) current++;
        return previous();
    }

    private boolean match(int current, int... types) {
        for (int type : types) {
            if (tokenStream.get(current).getType() == type) {
                return true;
            }
        }
        return false;
    }
}
