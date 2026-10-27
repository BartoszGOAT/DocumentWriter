<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Collaborative Editor</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <section class="hero is-primary">
        <div class="hero-body">
            <div class="container">
                <h1 class="title">Collaborative Document Editor</h1>
                <h2 class="subtitle">Login to your account</h2>
            </div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <div class="columns is-centered">
                <div class="column is-5-tablet is-4-desktop">
                    <div class="box">
                        <c:if test="${not empty error}">
                            <div class="notification is-danger">
                                ${error}
                            </div>
                        </c:if>
                        <c:if test="${not empty success}">
                            <div class="notification is-success">
                                ${success}
                            </div>
                        </c:if>
                        
                        <form action="${pageContext.request.contextPath}/auth/login" method="post">
                            <div class="field">
                                <label class="label">Username</label>
                                <div class="control">
                                    <input class="input" type="text" name="username" placeholder="Enter your username" required>
                                </div>
                            </div>
                            
                            <div class="field">
                                <label class="label">Password</label>
                                <div class="control">
                                    <input class="input" type="password" name="password" placeholder="Enter your password" required>
                                </div>
                            </div>
                            
                            <div class="field">
                                <div class="control">
                                    <button type="submit" class="button is-primary is-fullwidth">Login</button>
                                </div>
                            </div>
                        </form>
                        
                        <div class="has-text-centered mt-4">
                            <p>Don't have an account? <a href="${pageContext.request.contextPath}/auth/register">Register</a></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <footer class="footer">
        <div class="content has-text-centered">
            <p>
                <strong>Collaborative Document Editor</strong> by L3 Informatique.
            </p>
        </div>
    </footer>
</body>
</html>