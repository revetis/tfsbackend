package com.example.apps.shipments.views; // Paket yolunuzun doğruluğundan emin olun efendim

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("admin")
@AnonymousAllowed
public class AdminPanel extends VerticalLayout {
    public AdminPanel() {
        add(new H1("Efendim, Zafer Sayfanıza Hoş Geldiniz!"));
    }
}