# from django.contrib.auth.models import User, check_password
# class EmailAuthBackend(object):
# 	"""
# 	Email Authentication Backend

# 	Allows a user to sign in using an email/password pair rather than
# 	a username/password pair.
# 	"""
 
# 	def authenticate(self, username=None, password=None):
# 		""" Authenticate a user based on email address as the user name. """
# 		try:
# 			user = User.objects.get(email=email)
# 			if user.check_password(password):
# 				return email
# 			except User.DoesNotExist:
# 				return None
 
# 	def get_user(self, user_id):
# 		""" Get a User object from the user_id. """
# 		try:
# 			return User.objects.get(pk=user_id)
# 	except User.DoesNotExist:
# 		return None

from django.contrib.auth.backends import ModelBackend

from .models import User


class EmailAuthBackend(object):
    """
    Custom Email Backend to perform authentication via email
    """
    def authenticate(self, request, username, password):
        try:
            user = User.objects.get(email=username)
            if user.check_password(password): # check valid password
                return user # return user to be authenticated
        except User.DoesNotExist: # no matching user exists 
            return None 

    def get_user(self, user_id):
        try:
            return User.objects.get(pk=user_id)
        except:
            return None