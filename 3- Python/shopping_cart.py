from errors import ItemAlreadyExistsError, ItemNotExistError
from item import Item


class ShoppingCart:
    def __init__(self):
        self.items = {} #Dictionary to store all the items in the Shopping cart.

    def add_item(self, item: Item):
        if item.name in self.items:
            raise ItemAlreadyExistsError(f"Item '{item.name}' already exists in the shopping cart.")

        self.items[item.name] = item  # Adds the item to the Shopping cart.

    def remove_item(self, item_name: str):
        if item_name not in self.items:
            raise ItemNotExistError(f"Item '{item_name}' doesn't exist in the shopping cart.")

        del self.items[item_name]  # Remove the item from the dictionary.

    def get_subtotal(self) -> int:
        subtotal = 0
        for item in self.items.values():
            subtotal += item.price

        return subtotal

