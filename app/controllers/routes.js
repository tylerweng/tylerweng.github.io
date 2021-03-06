import passport from 'passport';
import express from 'express';
import * as usersController from './users.controller';
import * as badgesController from './badges.controller';
import requireLogin from '../middlewares/require.login';

const router = express.Router();

router.route('/users').get(usersController.getUsers);
router.route('/profile')
  .get(usersController.getUser)
  .delete(usersController.deleteUser);

router.route('/signup').post((req, res, next) => {
  passport.authenticate('local-register', (err, user, info) => {
    if (err) {
      res.status(500).send(err);
    } else if (!user) {
      res.status(401).json(info);
    } else {
      res.status(200).json(user);
    }
  })(req, res, next);
});

router.route('/login').post((req, res, next) => {
  passport.authenticate('local-signin', (err, user, info) => {
    if (err) {
      res.status(500).send(err);
    } else if (!user) {
      res.status(401).json(info);
    } else {
      res.status(200).json(user);
    }
  })(req, res, next);
});

router.route('/addCoins').post(usersController.addCoins);
router.route('/addLandmark').post(usersController.addLandmark);

export default router;
