package com.ttn.reapProject.event;

import com.ttn.reapProject.entity.Item;
import com.ttn.reapProject.entity.Role;
import com.ttn.reapProject.entity.User;
import com.ttn.reapProject.repository.ItemRepository;
import com.ttn.reapProject.repository.UserRepository;
import com.ttn.reapProject.service.ItemService;
import com.ttn.reapProject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class Bootstrap {
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemService itemService;

    @EventListener(ContextRefreshedEvent.class)
    void setUp() {
        if (!userRepository.findAll().iterator().hasNext()) {
            System.out.println("Bootstrapping admin data");
            User admin = new User();
            admin.setActive(true);
            admin.setEmail("vishal.aggarwal@tothenew.com");
            admin.setFirstName("Vishal");
            admin.setLastName("Aggarwal");
            admin.setFullName(admin.getFirstName() + " " + admin.getLastName());
            admin.setPassword("vishal");
            Set roleSet = new HashSet<Role>();
            roleSet.add(Role.ADMIN);
            roleSet.add(Role.PRACTICE_HEAD);
            admin.setRoleSet(roleSet);
            userService.save(admin);

            System.out.println("Bootstrapping first user data");
            User user1 = new User();
            user1.setActive(true);
            user1.setEmail("vishalaggarwal783@gmail.com");
            user1.setFirstName("Test");
            user1.setLastName("User");
            user1.setFullName(user1.getFirstName() + " " + user1.getLastName());
            user1.setPassword("test123");
            Set roleSet1 = new HashSet<Role>();
            roleSet1.add(Role.SUPERVISOR);
            user1.setRoleSet(roleSet1);
            userService.save(user1);
            // System.out.println(user1.toString());
        }
        if (!itemRepository.findAll().iterator().hasNext()) {
            Item item1 = new Item();
            item1.setName("Tshirt");
            item1.setPointsWorth(100);
            item1.setQuantity(50);
            item1.setImageUrl("/images/tshirt.jpg");
            itemService.save(item1);

            Item item2 = new Item();
            item2.setName("Cap");
            item2.setPointsWorth(70);
            item2.setQuantity(100);
            item2.setImageUrl("/images/cap.jpg");
            itemService.save(item2);

            Item item3 = new Item();
            item3.setName("Backpack");
            item3.setPointsWorth(150);
            item3.setQuantity(70);
            item3.setImageUrl("/images/backpack.jpg");
            itemService.save(item3);

            Item item4 = new Item();
            item4.setName("Bottle");
            item4.setPointsWorth(80);
            item4.setQuantity(100);
            item4.setImageUrl("/images/bottle.jpeg");
            itemService.save(item4);

            Item item5 = new Item();
            item5.setName("Desk Vase");
            item5.setPointsWorth(200);
            item5.setQuantity(30);
            item5.setImageUrl("/images/deskvase.jpg");
            itemService.save(item5);

            Item item6 = new Item();
            item6.setName("Keychain");
            item6.setPointsWorth(30);
            item6.setQuantity(150);
            item6.setImageUrl("/images/keychain.jpg");
            itemService.save(item6);

            Item item7 = new Item();
            item7.setName("Spiral Notebook + Pen Set");
            item7.setPointsWorth(40);
            item7.setQuantity(100);
            item7.setImageUrl("/images/notebook.jpg");
            itemService.save(item7);

            Item item8 = new Item();
            item8.setName("Passport/Travel Wallet");
            item8.setPointsWorth(130);
            item8.setQuantity(50);
            item8.setImageUrl("/images/passport-wallet.jpg");
            itemService.save(item8);

            Item item9 = new Item();
            item9.setName("Stationery Organizer");
            item9.setPointsWorth(50);
            item9.setQuantity(100);
            item9.setImageUrl("/images/stationery-organizer.jpg");
            itemService.save(item9);

            Item item10 = new Item();
            item10.setName("10000 mAh Power Bank");
            item10.setPointsWorth(200);
            item10.setQuantity(60);
            item10.setImageUrl("/images/power-bank.jpg");
            itemService.save(item10);
        }
    }
}
