/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Schema;

import javax.ws.rs.ApplicationPath;
import org.springframework.stereotype.Component;

/**
 *
 * @author Netto Léa
 */
public class JerseyConfig {

    @Component
    @ApplicationPath("/adventureisis")
    public class JerseyConfig extends ResourceConfig {

        public JerseyConfig() {

            register(Webservices.class);
        }
    }

}
