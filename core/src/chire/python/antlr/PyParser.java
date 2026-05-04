package chire.python.antlr;

import chire.python.util.type.TypeChecker;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
                    return new PyStatement.ForStatement(variable, iterable, body);

                default:
                    body.addAll(bodyDeclaration());
                    break;
            }

            current++;
        }

        throw new RuntimeException("no key");
//        return new PyStatement.ForStatement(variable, iterable, bodyDeclaration(3));
    }

    private PyStatement importDeclaration() {
        return importDeclaration(0);
    }

    private PyStatement importDeclaration(int cur) {
        this.current += cur;

        var key = peek();
        PyStatement.ImportStatement importStatement = null;
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

                importStatement = new PyStatement.ImportStatement(path.toString(), key.getText());
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
                            importStatement = new PyStatement.ImportStatement(pack.toString(), key.getText());
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
            return new PyStatement.SubSetStatement(var, call, assignment(1));
        }

        if (last().getType() == 54) {
            return submethodCall(
                    new PyStatement.SubCallStatement(var, call)
            );
        }

        return new PyStatement.SubCallStatement(var, call);
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
                    return new PyStatement.ClassStatement(class_token, body);

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
                        body.add(new PyStatement.BreakStatement());
                        break;

                    case 37:
                        if (match(this.current + 1, 44)) {
                            body.add(new PyStatement.ReturnStatement());
                        } else {
                            body.add(new PyStatement.ReturnStatement(assignment(1)));
                        }
                        break;

                    case 2:
                        return new PyStatement.WhileStatement(ifStmt, body);

                    default:
                        break;
                }
            }

            this.current++;
        }

        return null;
    }

    private ArrayList<PyStatement.ArgStatement> argsDeclaration(){
        ArrayList<PyStatement.ArgStatement> args = new ArrayList<>();

        while (!isEnd()) {
            current++;

            var token = peek();

            switch (token.getType()) {
                case 45:
                    PyStatement.TypeStatement type;
                    if (match(this.current+1, 60)){
                        current+=2;
                        if (!match(current, 45, 3)) throw new RuntimeException("no key");
                        type = typeDeclaration();
                    } else {
                        type = null;
                    }

                    args.add(new PyStatement.ArgStatement(token, type));
                    break;

                case 59:
                    break;
                case 58:
                    return args;
            }
        }

        throw new RuntimeException("no args");
    }

    private PyStatement.FunStatement defDeclaration(){
        current++;

        var token = peek();

        if (token.getType() == 45) {
            ArrayList<PyStatement.ArgStatement> args = argsDeclaration();
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
                        return new PyStatement.FunStatement(token, args, body);

                    case 37:
                        if (match(this.current+1, 44)) {
                            body.add(new PyStatement.ReturnStatement());
                        } else {
                            body.add(new PyStatement.ReturnStatement(assignment(1)));
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

    private PyStatement.TypeStatement typeDeclaration(){
        return typeDeclaration(0);
    }
    private PyStatement.TypeStatement typeDeclaration(int cur){
        this.current += cur;

        Token token = peek();

        switch (token.getType()) {
            case 45, 3:
                return new PyStatement.TypeStatement(token);
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
                return new PyStatement.VarStatement(name, asm);

            case 60:
                name = previous();
                var type = typeDeclaration(1);
                current++;
                asm = assignment(1);
                return new PyStatement.VarStatement(name, asm, type);

            case 64:
                name = previous();
                var index = assignment(1);
                current += 2;
                if (match(current, 63)) {
                    asm = assignment(1);
                    return new PyStatement.VarStatement(name, index, asm, null);
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

                return new PyStatement.VarStatement(previous(),
                        new PyStatement.LogicalStatement(
                                new PyStatement.VarCallStatement(previous()),
                                operator,
                                assignment(1)
                        ));

            case 44:
                //TODO 这里在创建时未处理空结构的问题。很明显，我应该默认None
                throw new RuntimeException("parser error in "+key);
//                return new PyStatement.VarStatement(previous(), null);

            default:
                throw new RuntimeException("parser error in "+key);
        }
    }

    private PyStatement ifDeclaration(){
        return ifDeclaration(0);
    }

    private PyStatement.IfStatement ifDeclaration(int cur){
        this.current += cur;

        var ifStmt = ifCondition();
        ArrayList<PyStatement> body = new ArrayList<>();
        PyStatement.IfStatement elseStmt = null;

        while (!isEnd()) {
            if (match(current, bodyIndex)) {
                body.addAll(bodyDeclaration());
            } else {
                switch (peek().getType()) {
                    case 44, 1:
                        break;

                    case 37:
                        if (match(this.current + 1, 44)) {
                            body.add(new PyStatement.ReturnStatement());
                        } else {
                            body.add(new PyStatement.ReturnStatement(assignment(1)));
                        }
                        break;

                    case 11:
                        body.add(new PyStatement.BreakStatement());
                        break;

                    case 2:
                        if (last().getType() == 17) {
                            elseStmt = ifDeclaration(1);
                        } else if (last().getType() == 18) {
                            elseStmt = ifDeclaration(1);
                        }

                        return new PyStatement.IfStatement(ifStmt, body, elseStmt);

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
                        return new PyStatement.JudgmentStatement(left, operator, right);
                    } else if (operator == null && right == null){
                        return left;
                    } else {
                        throw new RuntimeException("no key?");
                    }

                case 38, 20:
                    var key = peek();
                    if (Objects.equals(key.getText(), "True")) {
                        return new PyStatement.ConstStatement<>(key, Boolean.class);
                    } else if (Objects.equals(key.getText(), "False")){
                        return new PyStatement.ConstStatement<>(key, Boolean.class);
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
                return new PyStatement.ConstStatement<>(key, String.class);
            case 4, 72, 71:
                boolean range = true;

                if (match(current, 71, 72)) {
                    range = peek().getType() != 72;
                    current++;
                    key = peek();
                }

                PyStatement constStmt;

                if (TypeChecker.isInteger(key.getText())) {
                    constStmt = new PyStatement.NumberStatement<>(range, key, Integer.class);
                } else if (TypeChecker.isFloatingPointNumber(key.getText())) {
                    constStmt = new PyStatement.NumberStatement<>(range, key, Float.class);
                } else {
                    throw new RuntimeException("parser error "+peek());
                }

                if (match(this.current+1, 71, 72)) {
                    current++;
                    return new PyStatement.LogicalStatement(constStmt, peek(), assignment(1));
                } else if (match(this.current+1, 73, 56)){
                    current++;
                    var lgc = new PyStatement.LogicalStatement(constStmt, peek(), logicalAssignment(1));

                    if (match(this.current+1, 71, 72, 73, 56)) {
                        current++;
                        return new PyStatement.LogicalStatement(
                                lgc,
                                peek(),
                                assignment(1)
                        );
                    } else {
                        return lgc;
                    }
                } else {
                    return constStmt;
                }
            case 38, 20:
                if (Objects.equals(key.getText(), "True")) {
                    return new PyStatement.ConstStatement<>(key, Boolean.class);
                } else if (Objects.equals(key.getText(), "False")){
                    return new PyStatement.ConstStatement<>(key, Boolean.class);
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
                    varmetStmt = new PyStatement.IndexStatement(key, assignment(1));

                    if (last().getType() == 65) current++;
                    else throw new RuntimeException("no key");
                }
                else {
                    varmetStmt = varCall();
                }

                if (match(this.current+1, 71, 72)) {
                    current++;
                    return new PyStatement.LogicalStatement(varmetStmt, peek(), assignment(1));
                } else if (match(this.current+1, 73, 56)) {
                    current++;
                    var lgc = new PyStatement.LogicalStatement(varmetStmt, peek(), logicalAssignment(1));

                    if (match(this.current + 1, 71, 72, 73, 56)) {
                        current++;
                        return new PyStatement.LogicalStatement(
                                lgc,
                                peek(),
                                assignment(1)
                        );
                    } else {
                        return lgc;
                    }
                } else if (match(current+1, 54)) {
                    return submethodCall(varCall());
                } else {
                    return varmetStmt;
                }

            case 31:
                return new PyStatement.NoneStatement();

            case 57:
                return tupleDeclaration();

            case 77:
                return dictDeclaration();

            default:
                throw new RuntimeException("parser error in "+key);
        }
    }

    private PyStatement logicalAssignment(){
        return logicalAssignment(0);
    }

    private PyStatement logicalAssignment(int cur) {
        this.current += cur;

        var key = peek();

        switch (key.getType()) {
            case 3:
                return new PyStatement.ConstStatement<>(key, String.class);
            case 4:
                if (TypeChecker.isInteger(key.getText())) {
                    return new PyStatement.NumberStatement<>(key, Integer.class);
                } else if (TypeChecker.isFloatingPointNumber(key.getText())) {
                    return new PyStatement.NumberStatement<>(key, Float.class);
                } else {
                    throw new RuntimeException("parser error");
                }

            case 45:
                if (match(this.current+1, 57)){
                    return methodCall();
                } else {
                    return varCall();
                }

            case 57:
                return assignment(1);

            default:
                throw new RuntimeException("parser error");
        }
    }

    private PyStatement varCall(){
        return varCall(0);
    }

    private PyStatement varCall(int current){
        this.current += current;
        return new PyStatement.VarCallStatement(peek());
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
                    return new PyStatement.DictStatement(args);
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
                    return new PyStatement.TupleStatement(args);
            }
        }

        throw new RuntimeException("no args");
    }

    private PyStatement listAssignment() {
        return new PyStatement.ListStatement(keyDeclaration());
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
                        args.add(new PyStatement.ParametersStatement(peek(), assignment(2)));
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

    private PyStatement.FunCallStatement methodCall(){
        var funCall = new PyStatement.FunCallStatement(peek());

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
