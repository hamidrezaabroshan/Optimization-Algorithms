import random

import numpy as np

from Item import Item


class Knapsack:
    def __init__(self,
                 items_count=10,
                 items_weight_lower_bound=1,
                 items_weight_upper_bound=100,
                 items_price_lower_bound=1,
                 items_price_upper_bound=20,
                 knapsack_size=100,
                 do_generate_items_random=False,
                 ):
        self.__items_count = items_count
        self.__items_weight_lower_bound = items_weight_lower_bound
        self.__items_weight_upper_bound = items_weight_upper_bound
        self.__items_price_lower_bound = items_price_lower_bound
        self.__items_price_upper_bound = items_price_upper_bound
        self.__knapsack_size = knapsack_size
        self.__items = []
        self.generate_items(do_generate_items_random)
        return

    def generate_items(self, do_generate_items_random):
        if do_generate_items_random:
            for i in range(self.__items_count):
                item = Item(
                    random.randrange(self.__items_weight_lower_bound, self.__items_weight_upper_bound),
                    random.randrange(self.__items_price_lower_bound, self.__items_price_upper_bound),
                )
                self.__items.append(item)
        else:
            self.__items.append(Item(5, 17))
            self.__items.append(Item(97, 17))
            self.__items.append(Item(88, 2))
            self.__items.append(Item(65, 11))
            self.__items.append(Item(1, 1))
            self.__items.append(Item(56, 11))
            self.__items.append(Item(85, 9))
            self.__items.append(Item(34, 14))
            self.__items.append(Item(91, 18))
            self.__items.append(Item(84, 11))
            self.__items_count = len(self.__items)
        return

    def print_items(self, items=None):
        if items is None:
            items = self.__items

        for i in range(self.__items_count):
            print("Item #" + str(i + 1) + " Weight: " + str(items[i].get_weight()) + " Price: " + str(
                items[i].get_price()))

        return

    def print_solution(self, solution):
        if solution is None:
            print("Solution is empty.")
            return
        t_str = ""
        for i in range(self.__items_count):
            t_str += "#" + str(i + 1) + ":" + str(solution[i]) + ", "

        print("Soulution is: [" + t_str + "]")
        return

    def generate_initial_solution(self):
        while True:
            selected_items_count = random.randrange(self.__items_count)
            solution = np.array([1] * selected_items_count +
                                [0] * (self.__items_count - selected_items_count))
            np.random.shuffle(solution)
            if self.is_solution_valid(solution):
                break

        return solution.copy()

    def generate_neighbour(self, solution):
        while True:
            flip_index = random.randrange(self.__items_count)
            solution[flip_index] ^= 1
            if self.is_solution_valid(solution):
                break

        return solution.copy()

    def calculate_sa_total_price(self, solution):
        if solution is None:
            print("Solution is empty.")
            return
        x = 0
        for i in range(self.__items_count):
            x += (self.__items[i]).get_price() if solution[i] else 0
        return x

    def calculate_qa_total_energy(self, solution_spin_variable, weight_spin_variables):
        if solution_spin_variable is None or weight_spin_variables is None:
            print("Solution is empty.")
            return
        (a, b) = self.calculate_hamiltonian_energy_parameters()
        # Calculate potential hamiltonian (ha)
        if len(np.nonzero(weight_spin_variables)[0]) > 0:
            current_knapsack_weight = np.nonzero(weight_spin_variables)[0][0]
        else:
            current_knapsack_weight = 0
        ha = a * (1 - sum(weight_spin_variables)) ** 2 + a * ((
                                                                      current_knapsack_weight - self.calculate_total_weight(
                                                                  solution_spin_variable)) ** 2)
        # Calculate kinetic hamiltonian (hb)
        hb = -1 * b * self.calculate_sa_total_price(solution_spin_variable)

        hamiltonian = ha + hb
        return hamiltonian

    def calculate_total_weight(self, solution=None):
        if solution is None:
            print("Solution is empty.")
            return
        x = 0
        for i in range(self.__items_count):
            x += ((self.__items[i]).get_weight() * solution[i])
        return x

    def is_solution_valid(self, solution):
        if self.calculate_total_weight(solution) < self.__knapsack_size:
            return True
        return False

    def get_knapsack_size(self):
        return self.__knapsack_size

    def calculate_hamiltonian_energy_parameters(self):
        return self.get_max_price() + 1, 1

    def get_max_price(self):
        max_price = 0
        for item in self.__items:
            if max_price < item.get_price():
                max_price = item.get_price()
        return max_price


if __name__ == "__main__":
    knapsack = Knapsack()
    knapsack.print_items()
    solution = knapsack.generate_initial_solution()
    knapsack.print_solution(solution)
    neighbor_solution = knapsack.generate_neighbour(solution)
    knapsack.print_solution(neighbor_solution)
    print(knapsack.calculate_sa_total_price(neighbor_solution))
