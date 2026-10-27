<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Collaborative Editor</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <section class="hero is-primary">
        <div class="hero-body">
            <div class="container">
                <h1 class="title">Collaborative Document Editor</h1>
                <h2 class="subtitle">Create a new account</h2>
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
                        
                        <form action="${pageContext.request.contextPath}/auth/register" method="post">
                            <div class="field">
                                <label class="label">Username</label>
                                <div class="control">
                                    <input class="input" type="text" name="username" placeholder="Choose a username" required>
                                </div>
                            </div>
                            
                            <div class="field">
                                <label class="label">Email</label>
                                <div class="control">
                                    <input class="input" type="email" name="email" placeholder="Enter your email" required>
                                </div>
                            </div>
                            
                            <div class="field">
                                <label class="label">Password</label>
                                <div class="control">
                                    <input class="input" type="password" name="password" placeholder="Choose a password" required>
                                </div>
                            </div>
                            
                            <div class="field">
                                <label class="label">Confirm Password</label>
                                <div class="control">
                                    <input class="input" type="password" name="confirmPassword" placeholder="Confirm your password" required>
                                </div>
                            </div>
                            
                            <div class="field">
                                <div class="control">
                                    <button type="submit" class="button is-primary is-fullwidth">Register</button>
                                </div>
                            </div>
                        </form>
                        
                        <div class="has-text-centered mt-4">
                            <p>Already have an account? <a href="${pageContext.request.contextPath}/auth/login">Login</a></p>
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