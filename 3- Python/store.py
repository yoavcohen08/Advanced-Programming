import yaml

import shopping_cart
from errors import ItemNotExistError, TooManyMatchesError, ItemAlreadyExistsError
from item import Item
from shopping_cart import ShoppingCart

class Store:
    def __init__(self, path):
        with open(path) as inventory:
            items_raw = yaml.load(inventory, Loader=yaml.FullLoader)['items']
        self._items = self._convert_to_item_objects(items_raw)
        self._shopping_cart = ShoppingCart()

    @staticmethod
    def _convert_to_item_objects(items_raw):
        return [Item(item['name'],
                     int(item['price']),
                     item['hashtags'],
                     item['description'])
                for item in items_raw]
    def get_items(self) -> list:
        return self._items

    def search_by_name(self, item_name: str) -> list:
        Tags =[]
        for cart_item in self._shopping_cart.items.values():  #Build Tags
            for Hashhash in cart_item.hashtags:
                Tags.append(Hashhash)

        currItems = {}
        for item in self._items:
            if item_name in item.name and item.name not in self._shopping_cart.items.keys():
                currItems[item] = 0  #Items that contain str and not in Shopping cart.


        for Item in currItems:   #Adds number of hashtags for each item
            counter = 0
            for Hash in Item.hashtags:
                counter += Tags.count(Hash)
            currItems[Item] = counter

        sorted_items = sorted(currItems.keys(), key=lambda item: (-currItems[item], item.name))  #Sorts
        return sorted_items

    def search_by_hashtag(self, hashtag: str) -> list:
        Tags = []
        for cart_item in self._shopping_cart.items.values(): #Build Tags
            for Hashhash in cart_item.hashtags:
                Tags.append(Hashhash)

        currItems = {}
        for item in self._items:
            if hashtag in item.hashtags and item.name not in self._shopping_cart.items.keys():
                currItems[item] = 0  # Items that contain str and not in Shopping cart.

        for Item in currItems:  # Adds number of hashtags for each item
            counter = 0
            for Hash in Item.hashtags:
                counter += Tags.count(Hash)
            currItems[Item] = counter

        sorted_items = sorted(currItems.keys(), key=lambda item: (-currItems[item], item.name))  # Sorts
        return sorted_items

    def add_item(self, item_name: str):
        Match = []
        for item in self._items:
            if item_name in item.name:  #check if names is in items
                Match.append(item)
        if len(Match) == 0:
            raise ItemNotExistError (f"No item with name containing '{item_name}' exists.")
        elif len(Match) > 1:
            raise TooManyMatchesError (f"Multiple items match the name substring '{item_name}'.")
        elif Match[0].name in self._shopping_cart.items:
            raise ItemAlreadyExistsError(f"Item '{Match[0].name}' is already in the shopping cart.")

        self._shopping_cart.add_item(Match[0])


    def remove_item(self, item_name: str):
        Match = []
        for item in self._shopping_cart.items.values(): #check if names is in shopping cart
            if item_name in item.name:
                Match.append(item)
        if len(Match) == 0:
            raise ItemNotExistError (f"No item with name containing '{item_name}' exists in the shopping cart.")
        elif len(Match) > 1:
            raise TooManyMatchesError (f"Multiple items match the name substring '{item_name}' exists in the shopping cart.")

        self._shopping_cart.remove_item(Match[0].name)

    def checkout(self) -> int:
        total = 0
        for item in self._shopping_cart.items.values():
            total += item.price
        return total


