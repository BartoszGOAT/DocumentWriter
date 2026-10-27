<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gonner Docs</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <section class="hero is-primary is-fullheight-with-navbar">
        <div class="hero-body">
            <div class="container">
                <div class="columns is-vcentered">
                    <div class="column is-5">
                        <h1 class="title is-1">Gooner Docs</h1>
                        <h2 class="subtitle is-3">Edit documents in real-time with your team</h2>
                        <div class="buttons mt-5">
                            <c:choose>
                                <c:when test="${empty sessionScope.userId}">
                                    <a href="${pageContext.request.contextPath}/auth/login" class="button is-light is-large">Login</a>
                                    <a href="${pageContext.request.contextPath}/auth/register" class="button is-primary is-inverted is-large">Register</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/dashboard" class="button is-light is-large">Go to Dashboard</a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <div class="column is-6 is-offset-1">
                        <figure class="image">
                            <img src="https://via.placeholder.com/800x500" alt="Collaboration illustration">
                        </figure>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <h2 class="title has-text-centered">Features</h2>
            <div class="columns mt-5">
                <div class="column">
                    <div class="box has-text-centered">
                        <h3 class="title is-4 mt-4">Real-time Collaboration</h3>
                        <p>Edit documents simultaneously with multiple users and see changes instantly.</p>
                    </div>
                </div>
                <div class="column">
                    <div class="box has-text-centered">
                        <h3 class="title is-4 mt-4">Built-in Chat</h3>
                        <p>Communicate with your team while working on documents together.</p>
                    </div>
                </div>
                <div class="column">
                    <div class="box has-text-centered">
                        <h3 class="title is-4 mt-4">Version History</h3>
                        <p>Keep track of document changes and revert to previous versions when needed.</p>
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