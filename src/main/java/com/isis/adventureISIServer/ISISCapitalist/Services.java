/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.isis.adventureISIServer.ISISCapitalist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Netto Léa
 */
public class Services {
    InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
    
    public World readWorldFromXml(String username) throws JAXBException {
        String filename = username + "-world.xml";

        try {
            File temp = new File(filename);
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            World world = (World) u.unmarshal(temp);
            return world;
        } catch (Exception e) {
           
            //Unmarwhaller
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            World world = (World) u.unmarshal(input);
            System.out.println(e);
            return world;
        }

    }

    public World getWorld(String username) throws JAXBException, FileNotFoundException {
        //recupere date courante
        //regarde le monde stocké
        //comparer la date de la dernière mise à jour
        //lastupdate si différent on save
        World world = readWorldFromXml(username);
        long timeCurrent = System.currentTimeMillis();
        long dateDerniere = world.getLastupdate();
        if(dateDerniere==timeCurrent){
             return world; 
        }
        world = majScore(world);
        world.setLastupdate(System.currentTimeMillis());
        
       // saveWorldToXml(world,username);
        return world; 
        
        

       
    }
    
     public World majScore(World world){

        List<ProductType> products = world.getProducts().getProduct();
        long timeCurrent = System.currentTimeMillis();
        long dateDerniere = world.getLastupdate();
        long delta = timeCurrent-dateDerniere;
        
        
        for (ProductType p : products) {
            if(p.isManagerUnlocked())  {
                int angelBonus = world.getAngelbonus();
                int tempsProduit=p.getVitesse();
                int nbreProduit= (int) (delta/tempsProduit);
                long tempsRestant=p.getVitesse()-(delta%tempsProduit);
                p.setTimeleft(tempsRestant);               
                double argent = p.getRevenu()*nbreProduit*(1+world.getActiveangels()*angelBonus/100);
               //double angeBonus = Math.pow(world.getAngelbonus(), world.getActiveangels());
               //double argent = p.getRevenu() * p.getQuantite() * angeBonus;
                world.setMoney(world.getMoney()+argent);
                world.setScore(world.getScore()+argent);
                long tempsrestant = tempsProduit * (nbreProduit + 1) - delta;
                p.setTimeleft(tempsrestant);
            }
            else{
                if(p.getTimeleft()!=0){
                    if(p.getTimeleft()<delta){
                        double angeBonus = Math.pow(world.getAngelbonus(), world.getActiveangels());
                        double argent = p.getRevenu() * p.getQuantite() * angeBonus;
                        double score = world.getScore();
                        world.setScore(score+argent);
                        double money = world.getMoney();
                        world.setMoney(money+argent);  
                        p.setTimeleft(0);
                        delta -= p.getTimeleft();
                    }
                    else{
                        long timeleft = p.getTimeleft();
                        p.setTimeleft(timeleft-delta);
                        delta = 0;
                    }
                }
                
            }
        }
        return world;
       
}

    public void saveWorldToXml(World world, String username) throws FileNotFoundException, JAXBException {
        String fileName = username + "-world.xml";
        OutputStream output = new FileOutputStream(fileName);
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        m.marshal(world, output);

    }
    
    public void majPallier(ProductType product, PallierType p){
        
                p.setUnlocked(true);
                if (p.getTyperatio()==TyperatioType.VITESSE){
                    int vitesse=product.getVitesse();
                    int ratio=(int)p.getRatio();
                    int v2=vitesse*ratio;
                    product.setVitesse(v2);
                }
                else{
                    double revenu=product.getRevenu();
                    double ratio=p.getRatio();
                    double r2=revenu*ratio;
                    product.setRevenu(r2);
                }
    }

    // prend en paramètre le pseudo du joueur et le produit
    // sur lequel une action a eu lieu (lancement manuel de production ou
    // achat d’une certaine quantité de produit)
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException, FileNotFoundException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le produit équivalent à celui passé en paramètre
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }
        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantité
            // achetée et mettre à jour la quantité de product
            double prix1 = product.getCout();
            double q = product.getCroissance();
            double prix2 = prix1 * ((1 - Math.pow(q, qtchange)) / (1 - q));
            double argent = world.getMoney();
            double argentRestant = argent - prix2;
            product.setQuantite(newproduct.getQuantite());
            System.out.println("quantité");
            world.setMoney(argentRestant);

        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production
            product.setTimeleft(product.getVitesse());
        }
        
        List<PallierType> palliers=(List<PallierType>) product.getPalliers().getPallier();
        for (PallierType p: palliers){
            if (p.isUnlocked()==false && product.getQuantite()>=p.getSeuil()){
                majPallier(product,p);
            }
        }
        // sauvegarder les changements du monde
        saveWorldToXml(world, username);
        return true;
    }

    private ProductType findProductById(World world, int id) {
        ProductType produit = null;
        List<ProductType> products = world.getProducts().getProduct();
        for (ProductType p : products) {
            if (p.getId() == id) {
                produit = p;
            }
        }
        return produit;
    }
    
    public double updateScore(String username) throws JAXBException, FileNotFoundException{
        World world= getWorld(username);
        return world.getScore();
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
// renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException, FileNotFoundException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            return false;
        }

        // débloquer ce manager
        manager.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);
        double cout= manager.getSeuil();
        double argent = world.getMoney();
        double argentRestant = argent - cout ;
        world.setMoney(argentRestant);

        // soustraire de l'argent du joueur le cout du manager
        // sauvegarder les changements au monde
        saveWorldToXml(world, username);
        return true;
    }

    private PallierType findManagerByName(World world, String name) {
        PallierType manager = null;
        List<PallierType> managers = world.getManagers().getPallier();
        for (PallierType m : managers) {
            if (m.getName().equals(name)) {
                manager = m;
            }
    }
        return manager;
    }
    
   public boolean updateUpgrade(String username, PallierType upgrade) throws JAXBException, FileNotFoundException{
       World world= getWorld(username);
       if (upgrade.isUnlocked()==false && world.getMoney()>=upgrade.getSeuil()){
           if (upgrade.getIdcible()==0){
               List<ProductType> products = world.getProducts().getProduct();
               for (ProductType p :products){
                   majPallier(p,upgrade);
               }
              return true;
           }
           else{
               ProductType p=findProductById(world,upgrade.getIdcible());
               majPallier(p, upgrade);
           return true;
           }
       }
       else{
           return false;
       }
   }
   
   public void deleteWorld(String username) throws JAXBException, FileNotFoundException {
       World world=getWorld(username);
       double angeActif=world.getActiveangels();
       double angeTotal=world.getTotalangels();
       double newAnges=nombreAnges(world);
       angeActif=angeActif+newAnges;
       angeTotal=angeTotal+newAnges;
       double score=world.getScore();
       
       JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
       InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
       World monde;
       monde = (World) u.unmarshal(input);
       
       monde.setActiveangels(angeActif);
       monde.setTotalangels(angeTotal);
       monde.setScore(score);
       saveWorldToXml(monde, username);
   }
   
   public double nombreAnges(World world){
       double nbAnge = Math.round(150 * Math.sqrt(world.getScore()/ Math.pow(10, 15))) - world.getTotalangels();
       return nbAnge;
   }
   
   public void angelUpgrade(String username, PallierType ange) throws JAXBException, FileNotFoundException{
       int angeSeuil=ange.getSeuil();
       World world= getWorld(username);
       double angeActif=world.getActiveangels();
       double newAngeActif=angeActif-angeSeuil;
       if(ange.getTyperatio()==TyperatioType.ANGE){
           int angeBonus=world.getAngelbonus();
           angeBonus+=angeBonus+ange.getRatio();
           world.setAngelbonus(angeBonus);
       }
       else{
           updateUpgrade(username, ange);
          }
       world.setActiveangels(newAngeActif);
   }
}


